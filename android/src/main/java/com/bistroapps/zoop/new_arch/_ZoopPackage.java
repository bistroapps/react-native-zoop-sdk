package com.bistroapps.zoop;

import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;
import com.facebook.react.TurboReactPackage;

/**
 * Created by heldersi on 10/4/23.
 */
public class ZoopPackage implements TurboReactPackage{

  @Nullable
  @Override
  public NativeModule getModule(String name, ReactApplicationContext reactContext) {
    if (name.equals(ZoopModule.NAME)) {
        return new ZoopModule(reactContext);
    } else {
        return null;
    }
  }

  @Override
  public ReactModuleInfoProvider getReactModuleInfoProvider() {
      return () -> {
          final Map<String, ReactModuleInfo> moduleInfos = new HashMap<>();
          moduleInfos.put(
                  ZoopModule.NAME,
                  new ReactModuleInfo(
                          ZoopModule.NAME,
                          ZoopModule.NAME,
                          false, // canOverrideExistingModule
                          false, // needsEagerInit
                          true, // hasConstants
                          false, // isCxxModule
                          true // isTurboModule
          ));
          return moduleInfos;
      };
  }
}
