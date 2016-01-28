/**************************************************************************************** 
 Copyright © 2003-2012 ZTEsoft Corporation. All rights reserved. Reproduction or       <br>
 transmission in whole or in part, in any form or by any means, electronic, mechanical <br>
 or otherwise, is prohibited without the prior written consent of the copyright owner. <br>
 ****************************************************************************************/
package com.lq.luckking.utils;

/** 
 * 微信红包辅助类，主要定义一些抢微信红包使用的关键字
 * <Description> <br> 
 *  
 * @author lei.qiang<br>
 * @version 1.0<br>
 * @taskId <br>
 * @CreateDate Jan 27, 2016 <br>
 * @since V1.0<br>
 * @see com.lq.luckking.utils <br>
 */
public final class WeChatEnvelopeHelper {
	/**
	 * 红包消息的关键字(普通红包和口令红包获取都是这个关键字)
	 */
	public static final String ENVELOPE_TEXT_KEY = "[微信红包]";
	/**
	 * 普通红包未拆开时的关键字
	 */
	public static final String ENVELOPE_UNCLICK_TEXT_KEY = "领取红包";
}
