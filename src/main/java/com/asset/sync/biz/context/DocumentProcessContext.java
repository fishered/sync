package com.asset.sync.biz.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author fisher
 * @date 2023-08-11: 15:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "childBuilder")
public class DocumentProcessContext extends BaseProcessContext{

    /**
     * document data
     */
    private List<?> data;

    @Override
    public String getDesc() {
        return "DocumentFormat";
    }
}
