package com.asset.sync.service.asset.service.impl;

import com.asset.sync.domain.AssetInfoEntity;
import com.asset.sync.service.asset.mapper.AssetInfoMapper;
import com.asset.sync.service.asset.service.AssetInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author fisher
 * @date 2023-08-28: 11:36
 */
@Service
public class AssetInfoServiceImpl extends ServiceImpl<AssetInfoMapper, AssetInfoEntity> implements AssetInfoService {
}
