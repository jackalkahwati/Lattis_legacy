

import Foundation

public struct Failure: Error, Codable {
    public let name: String
    public let message: String
}

// Mark - Predefined failure reasons
public extension Failure {
    static var emptyResponse: Failure {
        .init(name: "Empty Response", message: "Nor payload nor error found in response")
    }
    
    enum Reason: String, Codable {
        case emptyResponse = "Empty Response"
        case missingParameter = "MissingParameter"
        case bookingNotFound = "BookingNotFound"
        case sentinelLockOffline = "SentinelLockOffline"
        case resourceNotFound = "ResourceNotFound"
        case resourceNotAvailable = "ResourceNotAvailable"
        case getBikeDetails = "GetBikeDetails"
    }
}

