package org.rdengine.view.manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 如果BaseView再View栈第0位,需要接受BaseActivity的back按键，<br>
 * 可以在类名上声明@FirstBaseViewNeedBackEve<br>
 * nt注解 Created by CCCMAX on 2019/4/23.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FirstBaseViewNeedBackEvent
{
}
