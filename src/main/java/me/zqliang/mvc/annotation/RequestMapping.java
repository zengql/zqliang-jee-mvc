package me.zqliang.mvc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主要作用于类和方法，当作连接参数
 * @author zqliang
 *
 */
@Target({ElementType.TYPE,  ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

	/**
	 * 制定连接地址
	 * @return
	 */
	String value();
	
}
