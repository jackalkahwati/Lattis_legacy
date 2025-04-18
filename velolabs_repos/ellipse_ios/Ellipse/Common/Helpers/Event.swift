//
//  Event.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/27/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Crashlytics

enum Event {
    case logIn(Method, Bool)
    case signUp(Method, Bool)
    case custom(Method)
    case share(Method)
    
    enum Method: String {
        case phone = "Phone"
        case facebook = "Facebook"
        case error = "Error"
        case lock = "Lock"
        case crashAlert = "Crash alert"
        case theftAlert = "Theft alert"
        case logOut = "LogOut"
        case passwordRestore = "Password restore"
        case deleteLock = "Delete lock"
        case shared = "Shared"
        case sharingAccepted = "Sharing accepted"
        case unshared = "Unshared"
        case onboarding = "Onboarding"
        case pinSave = "Pin change"
        case rename = "Lock rename"
        case sensetivityChange = "Sensetivity change"
        case fwUpdate = "Firmware update"
    }
    
    enum Attribute {
        case status(String)
        case screen(String)
        case succeded(Bool)
        case value(String)
        
        var identifire: String {
            switch self {
            case .status(_):
                return "status"
            case .screen(_):
                return "screen"
            case .succeded(_):
                return "succeded"
            case .value(_):
                return "value"
            }
        }
        
        var value: Any {
            switch self {
            case .status(let value):
                return value
            case .screen(let value):
                return value
            case .succeded(let value):
                return value
            case .value(let value):
                return value
            }
        }
    }
}

func log(_ event: Event, error: Error? = nil, attributes: [Event.Attribute] = []) {
    var customAttributes: [String: Any] = [:]
    if let error = error {
        customAttributes["error"] = error.localizedDescription
    }
    for attr in attributes {
        customAttributes[attr.identifire] = attr.value
    }
    switch event {
    case let .logIn(method, success):
        Answers.logLogin(withMethod: method.rawValue, success: NSNumber(value: success), customAttributes: customAttributes)
    case let .signUp(method, success):
        Answers.logSignUp(withMethod: method.rawValue, success: NSNumber(value: success), customAttributes: customAttributes)
    case .custom(let method):
        Answers.logCustomEvent(withName: method.rawValue, customAttributes: customAttributes)
    case .share(let method):
        Answers.logShare(withMethod: method.rawValue, contentName: nil, contentType: nil, contentId: nil, customAttributes: customAttributes)
    }
}
