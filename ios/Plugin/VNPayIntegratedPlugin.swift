import UIKit
import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(VNPayIntegratedPlugin)
public class VNPayIntegratedPlugin: CAPPlugin {
    private let implementation = VNPayIntegrated()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        DispatchQueue.main.async {
            ViewController().openSDK(self: self.bridge.viewController)
        }
        
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
}
