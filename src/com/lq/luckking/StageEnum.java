/**
 * 
 */
package com.lq.luckking;

/**
 * 阶段枚举值
 * @author leiqiang
 *
 */
public enum StageEnum {
	/**
	 * 获取红包结束
	 */
	fetched(0),
	/**
	 * 正在获取红包
	 */
	fetching(1),
	/**
	 * 正在开红包
	 */
	opening(2),
	/**
	 * 已拆开
	 */
	opened(3);
	
	StageEnum(int stageCode) {
		
	}
}
