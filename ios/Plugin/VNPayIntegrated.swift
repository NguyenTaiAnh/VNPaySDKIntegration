import Foundation

@objc public class VNPayIntegrated: NSObject {
    @objc public func echo(_ value: String) -> String {
        print("")
        print(value)
        return value
    }
}
