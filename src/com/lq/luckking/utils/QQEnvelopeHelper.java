/**************************************************************************************** 
 Copyright © 2003-2012 ZTEsoft Corporation. All rights reserved. Reproduction or       <br>
 transmission in whole or in part, in any form or by any means, electronic, mechanical <br>
 or otherwise, is prohibited without the prior written consent of the copyright owner. <br>
 ****************************************************************************************/
package com.lq.luckking.utils;

/** 
 * QQ 红包辅助类，主要定义一些抢QQ 红包用的关键字
 * <Description> <br> 
 *  
 * @author lei.qiang<br>
 * @version 1.0<br>
 * @taskId <br>
 * @CreateDate Jan 27, 2016 <br>
 * @since V1.0<br>
 * @see com.lq.luckking.utils <br>
 */
public final class QQEnvelopeHelper {
	/**
	 * 红包消息的关键字(普通红包和口令红包获取都是这个关键字)
	 */
	public static final String ENVELOPE_TEXT_KEY = "[QQ红包]";
	/**
	 * 普通红包未拆开时的关键字
	 */
	public static final String ENVELOPE_UNCLICK_TEXT_KEY1 = "点击拆开";
	/**
	 * 红包 关键字(普通红包和口令红包)
	 */
	public static final String ENVELOPE_UNCLICK_TEXT_KEY2 = "QQ红包";
	/**
	 * 红包 关键字(自己发的口令红包)
	 */
	public static final String ENVELOPE_UNCLICK_TEXT_KEY3 = "查看领取详情";
	/**
	 * 普通个性化红包未拆开时的关键字1
	 */
	public static final String SPECIAL_ENVELOPE_UNCLICK_TEXT_KEY1 = "查看详情";
	/**
	 * 普通个性化红包未拆开时的关键字1
	 */
	public static final String SPECIAL_ENVELOPE_UNCLICK_TEXT_KEY2 = "QQ红包个性版";
	
	/**
	 * 口令红包未拆开时的关键字
	 */
	public static final String PASSWORD_ENVELOPE_UNCLICK_TEXT_KEY = "口令红包";
	/**
	 * 点击口令红包后的关键字输入按钮名称
	 */
	public static final String INPUT_PASSWORD_BUTTON_KEY = "点击输入口令";
	/**
	 * 聊天窗口唯一的发送按钮组件标识
	 */
	public static final String GLOBAL_SEND_BUTTON_KEY = "android.widget.Button";
	/**
	 * 聊天窗口唯一的消息输入组件标识
	 */
	public static final String GLOBAL_SEND_INPUT_KEY = "android.widget.EditText";
	/**
	 * 全局发送按钮的关键字
	 */
	public static final String GLOBAL_SENT_TEXT_KEY = "发送";

}
