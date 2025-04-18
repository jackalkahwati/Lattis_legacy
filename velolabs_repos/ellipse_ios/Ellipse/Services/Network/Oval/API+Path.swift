//
//  API+Path.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 29/09/2018.
//  Copyright © 2018 Lattis. All rights reserved.
//

import Oval

extension API {
    
    static func users(_ path: Path) -> API {
        return .init(path: "users/" + path.rawValue)
    }
    
    static func locks(_ path: Path) -> API {
        return .init(path: "locks/" + path.rawValue)
    }
    
    enum Path: String {
        // Common
        case registration
        
        // Users
        case getUser = "get-user"
        case updateUser = "update-user"
        case updatePasswordCode = "update-password-code"
        case updatePassword = "update-password"
        case updatePhoneNumberCode = "update-phone-number-code"
        case updatePhoneNumber = "update-phone-number"
        case deleteAccount = "delete-account"
        case forgotPasswordCode = "forgot-password-code"
        case confirmForgotPasswordCode = "confirm-forgot-password-code"
        case confirmUserCode = "confirm-user-code"
        case termsAndConditions = "terms-and-conditions"
        case acceptTermsAndConditions = "accept-terms-and-conditions"
        case checkAcceptedTermsAndConditions = "check-accepted-terms-and-conditions"
        case newTokens = "new-tokens"
        case signInCode = "sign-in-code"
        case updateEmailCode = "update-email-code"
        case updateEmail = "update-email"
        
        // Locks
        case usersLocks = "users-locks"
        case signedMessageAndPublicKey = "signed-message-and-public-key"
        case deleteLock = "delete-lock"
        case shareConfirmation = "share-confirmation"
        case firmwareVersions = "firmware-versions"
        case firmwareLog = "firmware-log"
        case firmware = "firmware"
        case savePinCode = "save-pin-code"
        case revokeSharing = "revoke-sharing"
        case updateLock = "update-lock"
        case share = "share"
        case sendEmergencyMessage = "send-emergency-message"
        case crashDetected = "crash-detected"
    }
    
    var conveniencePath: Path? {
        return Path(rawValue: url.lastPathComponent)
    }
}
