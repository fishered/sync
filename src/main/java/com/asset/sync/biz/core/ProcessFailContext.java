package com.asset.sync.biz.core;

import com.asset.sync.biz.context.MapProcessContext;
import com.asset.sync.biz.type.BizType;
import lombok.*;

/**
 * @author fisher
 * @date 2023-08-23: 15:33
 * 为什么不把data直接放进来？ 因为data占用太大了 这里面的元素可能会等待后执行
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ProcessFailContext {

    /**
     * 同步失败的数据类型
     */
    private BizType bizType;

    /**
     * 一次同步多少条
     */
    private Long count;

    /**
     * 第几片数据
     */
    private Integer scroll;

    /**
     * 存储的index
     */
    private String index;

    /**
     * context执行的上一次time 主要区别于历史恢复
     */
    private String lastTime;

    /**
     * 全量数据
     */
    @ToString.Exclude
    private MapProcessContext context;

}
