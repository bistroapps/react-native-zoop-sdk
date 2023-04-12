import { NativeModules } from 'react-native';
const { ReactNativeZoop } = NativeModules;
interface ReactNativeZoopInterface {
    initialize(): Promise<void>;
    startScan(): Promise<void>;
    stopScan(): Promise<void>;
    requestConnection(): Promise<void>;
    charge(): Promise<void>;
}
export default ReactNativeZoop as ReactNativeZoopInterface;