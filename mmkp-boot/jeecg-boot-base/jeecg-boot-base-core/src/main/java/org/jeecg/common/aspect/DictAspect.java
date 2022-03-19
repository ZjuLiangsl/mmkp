package org.jeecg.common.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: dangzhenghui
 * @Date: 2019-3-17 21:50
 * @Version: 1.0
 */
@Aspect
@Component
@Slf4j
public class DictAspect {

    @Autowired
    private CommonAPI commonAPI;
    @Autowired
    public RedisTemplate redisTemplate;

    @Pointcut("execution(public * org.platform.modules..*.*Controller.*(..))")
    public void excudeService() {
    }

    @Around("excudeService()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
    	long time1=System.currentTimeMillis();	
        Object result = pjp.proceed();
        long time2=System.currentTimeMillis();
        log.debug("JSON Data Time："+(time2-time1)+"ms");
        long start=System.currentTimeMillis();
        this.parseDictText(result);
        long end=System.currentTimeMillis();
        log.debug("Dict JSON Data  Time"+(end-start)+"ms");
        return result;
    }


    private void parseDictText(Object result) {
        if (result instanceof Result) {
            if (((Result) result).getResult() instanceof IPage) {
                List<JSONObject> items = new ArrayList<>();

                List<Field> dictFieldList = new ArrayList<>();
                Map<String, List<String>> dataListMap = new HashMap<>();

                for (Object record : ((IPage) ((Result) result).getResult()).getRecords()) {
                    ObjectMapper mapper = new ObjectMapper();
                    String json="{}";
                    try {
                         json = mapper.writeValueAsString(record);
                    } catch (JsonProcessingException e) {
                        log.error("json Exception"+e.getMessage(),e);
                    }
                    JSONObject item = JSONObject.parseObject(json);
                    for (Field field : oConvertUtils.getAllFields(record)) {
                        String value = item.getString(field.getName());
                        if (oConvertUtils.isEmpty(value)) {
                            continue;
                        }
                        if (field.getAnnotation(Dict.class) != null) {
                            if (!dictFieldList.contains(field)) {
                                dictFieldList.add(field);
                            }
                            String code = field.getAnnotation(Dict.class).dicCode();
                            String text = field.getAnnotation(Dict.class).dicText();
                            String table = field.getAnnotation(Dict.class).dictTable();

                            List<String> dataList;
                            String dictCode = code;
                            if (!StringUtils.isEmpty(table)) {
                                dictCode = String.format("%s,%s,%s", table, text, code);
                            }
                            dataList = dataListMap.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            this.listAddAllDeduplicate(dataList, Arrays.asList(value.split(",")));
                        }
                        if (field.getType().getName().equals("java.util.Date")&&field.getAnnotation(JsonFormat.class)==null&&item.get(field.getName())!=null){
                            SimpleDateFormat aDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            item.put(field.getName(), aDate.format(new Date((Long) item.get(field.getName()))));
                        }
                    }
                    items.add(item);
                }

                Map<String, List<DictModel>> translText = this.translateAllDict(dataListMap);

                for (JSONObject record : items) {
                    for (Field field : dictFieldList) {
                        String code = field.getAnnotation(Dict.class).dicCode();
                        String text = field.getAnnotation(Dict.class).dicText();
                        String table = field.getAnnotation(Dict.class).dictTable();

                        String fieldDictCode = code;
                        if (!StringUtils.isEmpty(table)) {
                            fieldDictCode = String.format("%s,%s,%s", table, text, code);
                        }

                        String value = record.getString(field.getName());
                        if (oConvertUtils.isNotEmpty(value)) {
                            List<DictModel> dictModels = translText.get(fieldDictCode);
                            if(dictModels==null || dictModels.size()==0){
                                continue;
                            }

                            String textValue = this.translDictText(dictModels, value);


                            record.put(field.getName() + CommonConstant.DICT_TEXT_SUFFIX, textValue);
                        }
                    }
                }

                ((IPage) ((Result) result).getResult()).setRecords(items);
            }

        }
    }

    /**
     */
    private void listAddAllDeduplicate(List<String> dataList, List<String> addList) {
        List<String> filterList = addList.stream().filter(i -> !dataList.contains(i)).collect(Collectors.toList());
        dataList.addAll(filterList);
    }


    private Map<String, List<DictModel>> translateAllDict(Map<String, List<String>> dataListMap) {
        Map<String, List<DictModel>> translText = new HashMap<>();
        List<String> needTranslData = new ArrayList<>();
        for (String dictCode : dataListMap.keySet()) {
            List<String> dataList = dataListMap.get(dictCode);
            if (dataList.size() == 0) {
                continue;
            }
            List<String> needTranslDataTable = new ArrayList<>();
            for (String s : dataList) {
                String data = s.trim();
                if (data.length() == 0) {
                    continue;
                }
                if (dictCode.contains(",")) {
                    String keyString = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", dictCode, data);
                    if (redisTemplate.hasKey(keyString)) {
                        try {
                            String text = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                            List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            list.add(new DictModel(data, text));
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }
                    } else if (!needTranslDataTable.contains(data)) {
                        needTranslDataTable.add(data);
                    }
                } else {
                    String keyString = String.format("sys:cache:dict::%s:%s", dictCode, data);
                    if (redisTemplate.hasKey(keyString)) {
                        try {
                            String text = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                            List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                            list.add(new DictModel(data, text));
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }
                    } else if (!needTranslData.contains(data)) {
                        needTranslData.add(data);
                    }
                }

            }
            if (needTranslDataTable.size() > 0) {
                String[] arr = dictCode.split(",");
                String table = arr[0], text = arr[1], code = arr[2];
                String values = String.join(",", needTranslDataTable);
                log.info("translateDictFromTableByKeys.dictCode:" + dictCode);
                log.info("translateDictFromTableByKeys.values:" + values);
                List<DictModel> texts = commonAPI.translateDictFromTableByKeys(table, text, code, values);
                log.info("translateDictFromTableByKeys.result:" + texts);
                List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                list.addAll(texts);

                for (DictModel dict : texts) {
                    String redisKey = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", dictCode, dict.getValue());
                    try {
                        redisTemplate.opsForValue().set(redisKey, dict.getText());
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }

        if (needTranslData.size() > 0) {
            List<String> dictCodeList = Arrays.asList(dataListMap.keySet().toArray(new String[]{}));
            List<String> filterDictCodes = dictCodeList.stream().filter(key -> !key.contains(",")).collect(Collectors.toList());
            String dictCodes = String.join(",", filterDictCodes);
            String values = String.join(",", needTranslData);
            log.info("translateManyDict.dictCodes:" + dictCodes);
            log.info("translateManyDict.values:" + values);
            Map<String, List<DictModel>> manyDict = commonAPI.translateManyDict(dictCodes, values);
            log.info("translateManyDict.result:" + manyDict);
            for (String dictCode : manyDict.keySet()) {
                List<DictModel> list = translText.computeIfAbsent(dictCode, k -> new ArrayList<>());
                List<DictModel> newList = manyDict.get(dictCode);
                list.addAll(newList);

                for (DictModel dict : newList) {
                    String redisKey = String.format("sys:cache:dict::%s:%s", dictCode, dict.getValue());
                    try {
                        redisTemplate.opsForValue().set(redisKey, dict.getText());
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }
        return translText;
    }

    /**
     *
     * @param dictModels
     * @param values
     * @return
     */
    private String translDictText(List<DictModel> dictModels, String values) {
        List<String> result = new ArrayList<>();

        String[] splitVal = values.split(",");
        for (String val : splitVal) {
            String dictText = val;
            for (DictModel dict : dictModels) {
                if (val.equals(dict.getValue())) {
                    dictText = dict.getText();
                    break;
                }
            }
            result.add(dictText);
        }
        return String.join(",", result);
    }

    /**
     * @param code
     * @param text
     * @param table
     * @param key
     * @return
     */
    @Deprecated
    private String translateDictValue(String code, String text, String table, String key) {
    	if(oConvertUtils.isEmpty(key)) {
    		return null;
    	}
        StringBuffer textValue=new StringBuffer();
        String[] keys = key.split(",");
        for (String k : keys) {
            String tmpValue = null;
            log.debug(" dict key : "+ k);
            if (k.trim().length() == 0) {
                continue;
            }
            if (!StringUtils.isEmpty(table)){
                log.info("--DictAspect------dicTable="+ table+" ,dicText= "+text+" ,dicCode="+code);
                String keyString = String.format("sys:cache:dictTable::SimpleKey [%s,%s,%s,%s]",table,text,code,k.trim());
                    if (redisTemplate.hasKey(keyString)){
                    try {
                        tmpValue = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }else {
                    tmpValue= commonAPI.translateDictFromTable(table,text,code,k.trim());
                }
            }else {
                String keyString = String.format("sys:cache:dict::%s:%s",code,k.trim());
                if (redisTemplate.hasKey(keyString)){
                    try {
                        tmpValue = oConvertUtils.getString(redisTemplate.opsForValue().get(keyString));
                    } catch (Exception e) {
                       log.warn(e.getMessage());
                    }
                }else {
                    tmpValue = commonAPI.translateDict(code, k.trim());
                }
            }

            if (tmpValue != null) {
                if (!"".equals(textValue.toString())) {
                    textValue.append(",");
                }
                textValue.append(tmpValue);
            }

        }
        return textValue.toString();
    }

}
