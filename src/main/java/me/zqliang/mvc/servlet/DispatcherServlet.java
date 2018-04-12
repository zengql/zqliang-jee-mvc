package me.zqliang.mvc.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.zqliang.core.util.StringUtil;
import me.zqliang.mvc.annotation.Controller;
import me.zqliang.mvc.annotation.RequestMapping;
import me.zqliang.mvc.core.HandleMapping;

/**
 * mvc 的入口，基于servlet
 * @author zqliang
 *
 */
public class DispatcherServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8417562892528476323L;
	
	private List<Class<?>> controllerClasses = new ArrayList<>();
	
	/**
	 * 此类会把类和方法映射到一个map中去
	 */
	private Map<String, HandleMapping> mapping = new HashMap<>();

	/**
	 * 重写父类的init方法，
	 *  这个方法再全局中只被执行一次
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("servlet配置初始化");
		super.init(config);
		//加载配置文件
		
		//初始化所有采用controller注解的类
		String path = this.getClass().getResource("/").getPath();
		doScanner(path, "");
		
		//把所有controller类通过反射实例化，并放入到ioc容器中
		doInstance();
		
		//初始化HandleMapping（将uri和method对应上，方便快速查找）
	}
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doDispatcher(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doDispatcher(req, resp);
	}
	
	/**
	 * 从项目根路径开始扫描
	 */
	private void doScanner(String root, String pkgName) {
		//判断路径正确，并确定存在包或者类
		root			= root.replaceAll("\\\\", "/");
		System.out.println("开始扫描："+root);
		File dir 	= new File(root);
		
		ClassLoader loader = this.getClass().getClassLoader();
		Class<?> cls = null;
		Class<Controller> controlerClass = Controller.class;
		File[] files = dir.listFiles();
		if (null == files || files.length<1) {
			return;
		}
		for (File file : files) {
			
			System.out.println("查到文件:"+file.getAbsolutePath());
			try {
			    if (file.isDirectory()) {
			    	String temPkgName = null;
			    	if (StringUtil.isEmpty(pkgName)) {
			    		temPkgName = file.getName();
			    	} else {
			    		temPkgName = "."+file.getName();
			    	}
					this.doScanner(root+temPkgName.replaceAll("\\.", "/"), pkgName+temPkgName);
				} else {
					boolean isClassFile = file.isFile() && "class".equals(this.getExtensionName(file.getName())); 
					if ( !isClassFile ) {//如果不是class文件不处理
						continue;
					}
					String className = file.getName().substring(0, file.getName().length()-6);
					cls = loader.loadClass(pkgName+"."+className);
					if ( cls.isAnnotationPresent(controlerClass) ) {
						System.out.println("找到controller:"+cls.getName());
						controllerClasses.add(cls);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
		
		/**
		 * 获取扩展名
		 * @param filename
		 * @return
		 */
		 private String getExtensionName(String filename) {   
		        if ((filename != null) && (filename.length() > 0)) {   
		        	int dot = filename.lastIndexOf('.');   
		            if ((dot >-1) && (dot < (filename.length() - 1))) {   
		                	return filename.substring(dot + 1);   
		            }   
		        }   
		       return filename;   
		 } 
	
		 /**
		  * 把所有controller的对外的接口进行处理
		  */
		 private void doInstance() {
			 
			 for ( Class<?> cls :  controllerClasses ) {
				 try {
					 Object controllerObj = cls.newInstance();
					 Method[] methods = cls.getMethods();//非public方法不处理
					 if ( null == methods || methods.length <1 ) {
						 continue;
					 }
					 
					 String baseUrl = "";
					 if ( cls.isAnnotationPresent(RequestMapping.class) ) {
						 baseUrl = cls.getAnnotation(RequestMapping.class).value();
					 }
					 String url = null;
					 for ( Method method : methods ) {
						 if (!method.isAnnotationPresent(RequestMapping.class)) {
							 continue;
						 }
						 url = method.getAnnotation(RequestMapping.class).value();
						 System.out.println("查出url:"+url+","+method.getName());
						 mapping.put( baseUrl+url, new HandleMapping(controllerObj, method) );
					 }
				 } catch (InstantiationException e) {
					 e.printStackTrace();
				 } catch (IllegalAccessException e) {
					 e.printStackTrace();
				 }
			 }
			 
		 }
		 
	
	
	private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) {
		resp.setCharacterEncoding("UTF-8");
		String uri = req.getRequestURI();
		HandleMapping mappingHandle = mapping.get(uri);
		if (null == mappingHandle) {
			return;
		}
		Method method = mappingHandle.getMethod();
		
		//组装参数
		Parameter[] params 		= method.getParameters();
		Object[] 	paramValues = new Object[params.length];
		for (int i = 0, paramsLength = params.length; i < paramsLength; i++) {
			if(ServletRequest.class.isAssignableFrom(params[i].getType())) {
				paramValues[i] = req;
			} else if(ServletResponse.class.isAssignableFrom(params[i].getType())) {
				paramValues[i] = resp;
			}  else {//其他类型只支持String, Integer ,float, double
				String bindValue = params[i].getName();
				String paramValue = req.getParameter(bindValue);
				
				if ( null != paramValue ) {
					Class<?> paramType = params[i].getType();
					if (Integer.class.isAssignableFrom(paramType)) {
						paramValues[i] = Integer.valueOf(paramValue);
					} else if (Float.class.isAssignableFrom(paramType)) {
						paramValues[i] = Float.valueOf(paramValue);
					} else if (Double.class.isAssignableFrom(paramType)) {
						paramValues[i] = Double.valueOf(paramValue);
					} else {
						paramValues[i] = paramValue;
					}
				}
				paramValues[i] = paramValue;
			}
		}
		
		try {
			method.invoke(mappingHandle.getController(), paramValues);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
}
