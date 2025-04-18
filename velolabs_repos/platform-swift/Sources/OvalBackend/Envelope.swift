

import Foundation


struct Envelope<Payload: Decodable>: Decodable {
    let payload: Payload?
    let error: Failure?
}

public struct EmptyJSON: Codable {
    public init() {}
}


public enum UploadType: String {
    case parking, maintenance, bike
}
