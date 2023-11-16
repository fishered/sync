package com.asset.sync.param.req;

import com.asset.sync.biz.context.BaseProcessContext;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author fisher
 * @date 2023-08-22: 16:06
 */
@Data
public class ManualSyncReq<P extends BaseProcessContext> {

    @NotNull(message = "定义param的参数不能为空！")
    private P p;

}
