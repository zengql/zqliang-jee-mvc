package me.zqliang.mvc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  只要给类添加此注解，就表明此类是个controller类
 * @author zqliang
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
	
	/**
	 * 设置别名，默认是“”
	 * @return
	 */
	String value() default "";
}
