//
//  Analitycs.swift
//  Operator
//
//  Created by Ravil Khusainov on 15.07.2021.
//

import Foundation
import FirebaseCrashlytics
import FirebaseAnalytics

struct Analytics {
    typealias FireLytics = FirebaseAnalytics.Analytics
    static let crashlitycs = Crashlytics.crashlytics()
    static func report(_ failure: Failure, with info: [Key: String?]? = nil, file: String = #file, function: String = #function, line: Int = #line) {
        let filename = file.split(separator: "/").compactMap(String.init).last ?? file
        var values: [String: String] = [:]
        for (key, value) in info ?? [:] where value != nil {
            values[key.rawValue] = value
        }
        if let desc = failure.description {
            crashlitycs.log(desc)
        }
        crashlitycs.setCustomKeysAndValues(values)
        let ex = ExceptionModel(name: failure.name, reason: failure.reason)
        ex.stackTrace = [
            .init(symbol: function, file: filename, line: line)
        ]
        crashlitycs.record(exceptionModel: ex)
    }
    
    static func set(user: Int) {
        let userId = String(user)
        crashlitycs.setUserID(userId)
        FireLytics.setUserID(userId)
    }
}

extension Analytics {
    enum Key: String {
        case vehicle
        case fleet
        case trip
    }
    
    enum Failure {
        case error(Error)
        case custom(String, String)
        
        var name: String {
            switch self {
            case .custom(let title, _):
                return title
            case .error:
                return "Error"
            }
        }
        
        var reason: String {
            switch self {
            case .custom(_, let reason):
                return reason
            case .error:
                return "Find the reason in logs"
            }
        }
        
        var description: String? {
            switch self {
            case .error(let error):
                return error.localizedDescription
            default:
                return nil
            }
        }
    }
}
