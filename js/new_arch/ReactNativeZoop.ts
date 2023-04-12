import type { TurboModule } from 'react-native/Libraries/TurboModule/RCTExport';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
    initialize(): Promise<void>;
    startScan(): Promise<void>;
    stopScan(): Promise<void>;
    requestConnection(): Promise<void>;
    charge(): Promise<void>;
}

export default TurboModuleRegistry.get<Spec>(
    'ReactNativeZoop'
) as Spec | {
    initialize(): Promise<void>;
    startScan(): Promise<void>;
    stopScan(): Promise<void>;
    requestConnection(): Promise<void>;
    charge(): Promise<void>;
}