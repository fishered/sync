package com.asset.sync.param.req;

import com.asset.sync.biz.type.BizType;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author fisher
 * @date 2023-08-23: 14:44
 */
@Data
public class ManualSyncAutoReq extends ManualSyncReq{

    @NotNull(message = "bizType不能为空！")
    private BizType bizType;

}
