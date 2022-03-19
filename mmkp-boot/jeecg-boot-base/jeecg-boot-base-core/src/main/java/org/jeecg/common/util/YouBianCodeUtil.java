package org.jeecg.common.util;

import io.netty.util.internal.StringUtil;

/**
 * A001
 * A001A002
 * @Author zhangdaihao
 *
 */
public class YouBianCodeUtil {


	private static final int numLength = 2;

	public static final int zhanweiLength = 1+numLength;

	/**
	 *
	 * @param code
	 * @return
	 */
	public static synchronized String getNextYouBianCode(String code) {
		String newcode = "";
		if (oConvertUtils.isEmpty(code)) {
			String zimu = "A";
			String num = getStrNum(1);
			newcode = zimu + num;
		} else {
			String before_code = code.substring(0, code.length() - 1- numLength);
			String after_code = code.substring(code.length() - 1 - numLength,code.length());
			char after_code_zimu = after_code.substring(0, 1).charAt(0);
			Integer after_code_num = Integer.parseInt(after_code.substring(1));
//			org.jeecgframework.core.util.LogUtil.info(after_code);
//			org.jeecgframework.core.util.LogUtil.info(after_code_zimu);
//			org.jeecgframework.core.util.LogUtil.info(after_code_num);

			String nextNum = "";
			char nextZimu = 'A';
			if (after_code_num == getMaxNumByLength(numLength)) {
				nextNum = getNextStrNum(0);
			} else {
				nextNum = getNextStrNum(after_code_num);
			}
			if(after_code_num == getMaxNumByLength(numLength)) {
				nextZimu = getNextZiMu(after_code_zimu);
			}else{
				nextZimu = after_code_zimu;
			}

			if ('Z' == after_code_zimu && getMaxNumByLength(numLength) == after_code_num) {
				newcode = code + (nextZimu + nextNum);
			} else {
				newcode = before_code + (nextZimu + nextNum);
			}
		}
		return newcode;

	}


	public static synchronized String getSubYouBianCode(String parentCode,String localCode) {
		if(localCode!=null && localCode!=""){

//			return parentCode + getNextYouBianCode(localCode);
			return getNextYouBianCode(localCode);

		}else{
			parentCode = parentCode + "A"+ getNextStrNum(0);
		}
		return parentCode;
	}

	


	private static String getNextStrNum(int num) {
		return getStrNum(getNextNum(num));
	}


	private static String getStrNum(int num) {
		String s = String.format("%0" + numLength + "d", num);
		return s;
	}


	private static int getNextNum(int num) {
		num++;
		return num;
	}


	private static char getNextZiMu(char zimu) {
		if (zimu == 'Z') {
			return 'A';
		}
		zimu++;
		return zimu;
	}
	

	private static int getMaxNumByLength(int length){
		if(length==0){
			return 0;
		}
		String max_num = "";
		for (int i=0;i<length;i++){
			max_num = max_num + "9";
		}
		return Integer.parseInt(max_num);
	}
	public static String[] cutYouBianCode(String code){
		if(code==null || StringUtil.isNullOrEmpty(code)){
			return null;
		}else{
			int c = code.length()/(numLength+1);
			String[] cutcode = new String[c];
			for(int i =0 ; i <c;i++){
				cutcode[i] = code.substring(0,(i+1)*(numLength+1));
			}
			return cutcode;
		}
		
	}
//	public static void main(String[] args) {
//		// org.jeecgframework.core.util.LogUtil.info(getNextZiMu('C'));
//		// org.jeecgframework.core.util.LogUtil.info(getNextNum(8));
//	    // org.jeecgframework.core.util.LogUtil.info(cutYouBianCode("C99A01B01")[2]);
//	}
}
