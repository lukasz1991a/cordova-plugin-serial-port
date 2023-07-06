cordova.define("com.plugin.SerialPortPlugin.SerialPortPlugin", function (require, exports, module) {
    let exec = require('cordova/exec');
    module.exports = class {
        constructor() {
            exec((a) => this.activeConnectionNumber = (a - 1), null, "SerialPortPlugin", "create", []);
        }

        openDevice(settings, success, error) {
            let waiting = setInterval(() => {
                if (!Number.isInteger(this.activeConnectionNumber)) return;
                clearInterval(waiting)
                exec(success, error, "SerialPortPlugin", "openDevice", [settings, this.activeConnectionNumber]);
            }, 300)
        }

        closeDevice(success, error) {
            exec(success, error, "SerialPortPlugin", "closeDevice", [this.activeConnectionNumber]);
        }

        read(success, error) {
            exec(success, error, "SerialPortPlugin", "read", [this.activeConnectionNumber]);
        }

        write(data, success, error) {
            exec(success, error, "SerialPortPlugin", "write", [data, this.activeConnectionNumber]);
        }

        sendDataAndWaitResponse(data, timeoutMs, success, error) {
            exec(success, error, "SerialPortPlugin", "sendDataAndWaitResponse", [data, timeoutMs, this.activeConnectionNumber]);
        }

        setHex(isTrue) {
            exec(null, null, "SerialPortPlugin", "setHex", [isTrue, this.activeConnectionNumber]);
        }
    };

});
