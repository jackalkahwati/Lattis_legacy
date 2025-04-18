
import Foundation
import HTTPClient


public struct Endpoint {
    
    let version: Version
    let basePath: BasePath
    let path: String?
    let query: [URLQueryItem]
    
    public init(version: Version = .v0, _ base: Endpoint.BasePath, path: String? = nil, query: [URLQueryItem] = []) {
        self.version = version
        self.basePath = base
        self.query = query
        self.path = path
    }
}

public extension Endpoint {
    var fullPath: String {
        var postfix = "api/\(basePath.rawValue)"
        if let path = path {
            postfix += "/\(path)"
        }
        switch version {
        case .v0:
            return postfix
        default:
            return version.rawValue + "/" + postfix
        }
    }
}

public extension Endpoint {
    enum BasePath: String {
        case users
        case rentals
        case bookings
        case apps
        case trips
        case bikes
        case hubs
        case sas
        case sentinel
        case subscriptions
        case memberships
        case equipment
    }
}
