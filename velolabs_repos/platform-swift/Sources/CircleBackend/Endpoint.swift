

import Foundation

public struct Endpoint {
    
    let basePath: BasePath
    let path: String?
    let query: [URLQueryItem]
    
    public init(_ base: Endpoint.BasePath, path: String? = nil, query: [URLQueryItem] = []) {
        self.basePath = base
        self.query = query
        self.path = path
    }
    
    public var fullPath: String {
        var full = basePath.rawValue
        if let path = path {
            full = full + "/" + path
        }
        return full
    }
}

public extension Endpoint {
    enum BasePath: String {
        case users
        case things
        case locks
        case vehicles
        case trips
        case bookings
        case fleets
    }
}
