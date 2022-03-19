package org.jeecg.common.util;

/**
 * @Author
 */
public class MyClassLoader extends ClassLoader {
	public static Class getClassByScn(String className) {
		Class myclass = null;
		try {
			myclass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(className+" not found!");
		}
		return myclass;
	}

	//       ，
	public static String getPackPath(Object object) {
		//
		if (object == null) {
			throw new java.lang.IllegalArgumentException("      ！");
		}
		//       ，
		String clsName = object.getClass().getName();
		return clsName;
	}

	public static String getAppPath(Class cls) {
		//
		if (cls == null) {
			throw new java.lang.IllegalArgumentException("      ！");
		}
		ClassLoader loader = cls.getClassLoader();
		//       ，
		String clsName = cls.getName() + ".class";
		//
		Package pack = cls.getPackage();
		String path = "";
		//        ，
		if (pack != null) {
			String packName = pack.getName();
			//          Java    ，      JDK
			if (packName.startsWith("java.") || packName.startsWith("javax.")) {
				throw new java.lang.IllegalArgumentException("       ！");
			}
			//       ，       ，
			clsName = clsName.substring(packName.length() + 1);
			//            ，   ，           ，
			if (packName.indexOf(".") < 0) {
				path = packName + "/";
			} else {//            ，
				int start = 0, end = 0;
				end = packName.indexOf(".");
				while (end != -1) {
					path = path + packName.substring(start, end) + "/";
					start = end + 1;
					end = packName.indexOf(".", start);
				}
				path = path + packName.substring(start) + "/";
			}
		}
		//   ClassLoader getResource  ，
		java.net.URL url = loader.getResource(path + clsName);
		//  URL
		String realPath = url.getPath();
		//            "file:"
		int pos = realPath.indexOf("file:");
		if (pos > -1) {
			realPath = realPath.substring(pos + 5);
		}
		//                   ，
		pos = realPath.indexOf(path + clsName);
		realPath = realPath.substring(0, pos - 1);
		//          JAR     ，     JAR
		if (realPath.endsWith("!")) {
			realPath = realPath.substring(0, realPath.lastIndexOf("/"));
		}
		/*------------------------------------------------------------  
		 ClassLoader getResource     utf-8          ，
		           ，           ，  ，           
		       ，  ，   URLDecoder decode      ，         
		           
		-------------------------------------------------------------*/
		try {
			realPath = java.net.URLDecoder.decode(realPath, "utf-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return realPath;
	}// getAppPath    
}
