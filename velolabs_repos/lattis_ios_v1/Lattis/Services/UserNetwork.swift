//
//  UsersNetwork.swift
//  Lattis
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Oval
import KeychainSwift

protocol UserNetwork {
    func getStatus(completion: @escaping (Result<Status.Info, Error>) -> ())
    func getEmailVerificationCode(email: String?, accoutnType: String?, completion: @escaping (Result<Void, Error>) -> ())
    func getConfirmationCode(email: String, completion: @escaping (Result<Void, Error>) -> ())
    func confirm(forgot confirmation: User.Password, completion: @escaping (Result<Void, Error>) -> ())
    func change(password: User.UpdatePassword, completion: @escaping (Result<Void, Error>) -> ())
    func getUpdateCode(email: String, completion: @escaping (Result<Void, Error>) -> ())
    func getUpdateCode(phoneNumber: String, completion: @escaping (Result<Void, Error>) -> ())
    func update(email: User.Email, completion: @escaping (Result<Void, Error>) -> ())
    func getUser(completion: @escaping (Result<User, Error>) -> ())
    func deleteUser(completion: @escaping (Result<Void, Error>) -> ())
    func registration(user: User.Request, completion: @escaping (Result<(Int, Bool), Error>) -> ())
    func getTokens(userId: Int, password: String, completion: @escaping (Result<Void, Error>) -> ())
    func confirm(email: String?, code: String, accountType: String?, completion: @escaping (Result<Void, Error>) -> ())
    func update(user: User.Request, completion: @escaping (Result<User, Error>) -> ())
    func update(phone: User.Phone, completion: @escaping (Result<Void, Error>) -> ())
    func addPrivateNetwork(email: String, completion: @escaping (Result<Bool, Error>) -> ())
    func confirmVerification(code: String, completion: @escaping (Result<Int, Error>) -> ())
}

protocol CardsNetwork {
    func creteIntent(completion: @escaping (Result<String, Error>) -> ())
    func add(card: CreditCard, completion: @escaping (Result<Void, Error>) -> ())
    func getCards(completion: @escaping (Result<[CreditCard], Error>) -> ())
    func setPrimary(card: CreditCard, completion: @escaping (Result<Void, Error>) -> ())
    func delete(card: CreditCard, completion: @escaping (Result<Void, Error>) -> ())
}

fileprivate extension API {
    static func users(path: String) -> API {
        return .init(path: "users/" + path)
    }
    static let status = users(path: "get-current-status")
    static let verificatoin = users(path: "email-verification-code")
    static let restorePassword = users(path: "forgot-password")
    static let saveNewPassword = users(path: "confirm-forgot-password")
    static let updatePassword = users(path: "change-password")
    static let getEmailCode = users(path: "update-email-code")
    static let updateEmail = users(path: "update-email")
    static let privateNetwork = users(path: "add-private-account")
    static let user = users(path: "get-user")
    static let addCard = users(path: "add-cards")
    static let card = users(path: "add-cards?action=setup_intent")
    static let cards = users(path: "get-cards")
    static let setPrimary = users(path: "set-card-primary")
    static let deleteCard = users(path: "delete-card")
    static let registration = users(path: "registration")
    static let getTokens = users(path: "new-tokens")
    static let confirmEmail = users(path: "confirm-email-verification-code")
    static let updateUser = users(path: "update-user")
    static let updatePhone = users(path: "update-phone-number")
    static let getUpdatePhoneCode = users(path: "update-phone-number-code")
    static let deleteUser = users(path: "delete-account")
}

extension Session: UserNetwork {
    func getStatus(completion: @escaping (Result<Status.Info, Error>) -> ()) {
        send(.post(json: UIDevice.current, api: .status), completion: completion)
    }
    
    func getEmailVerificationCode(email: String?, accoutnType: String?, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Encodable {
            let userId: Int
            let accountType: String
            let email: String?
        }
        let params = Params(userId: storage.userId!, accountType: accoutnType ?? "main_account", email: email)
        send(.post(json: params, api: .verificatoin), completion: completion)
    }
    
    func getConfirmationCode(email: String, completion: @escaping (Result<Void, Error>) -> ()) {
        let params: [String: String] = ["email": email]
        send(.post(json: params, api: .restorePassword), completion: completion)
    }
    
    func confirm(forgot confirmation: User.Password, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: confirmation, api: .saveNewPassword), completion: completion)
    }
    
    func change(password: User.UpdatePassword, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: password, api: .updatePassword), completion: completion)
    }
    
    func getUpdateCode(email: String, completion: @escaping (Result<Void, Error>) -> ()) {
        let params: [String: String] = ["email": email]
        send(.post(json: params, api: .getEmailCode), completion: completion)
    }
    
    func update(email: User.Email, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: email, api: .updateEmail), completion: completion)
    }
    
    func addPrivateNetwork(email: String, completion: @escaping (Result<Bool, Error>) -> ()) {
        let params: [String: String] = ["email": email]
        struct Res: Decodable {
            let lattisAccount: [Fake]
            
            struct Fake: Decodable {
                let email: String
            }
        }
        send(.post(json: params, api: .privateNetwork)) { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                completion(.success(res.lattisAccount.isEmpty == false))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func getUser(completion: @escaping (Result<User, Error>) -> ()) {
        struct Wrap: Decodable {
            let user: User
            let privateAccount: [PrivateNetwork]?
        }
        send(.emptyPost(.user)) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                var user = wrap.user
                if let accs = wrap.privateAccount {
                    user.privateNetworks = accs
                }
                completion(.success(user))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func deleteUser(completion: @escaping (Result<Void, Error>) -> ()) {
        // Delete user functionality was removed from the app
        return completion(.success(()))
//        send(.get(.deleteUser), completion: completion)
    }
    
    func registration(user: User.Request, completion: @escaping (Result<(Int, Bool), Error>) -> ()) {
        KeychainSwift().clear()
        struct Res: Decodable {
            let userId: Int
            let verified: Bool
        }
        send(.post(json: user, api: .registration)) { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                self.storage.userId = res.userId
                completion(.success((res.userId, res.verified)))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func getTokens(userId: Int, password: String, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Encodable {
            let userId: Int
            let password: String
        }
        send(.post(json: Params(userId: userId, password: password), api: .getTokens)) { (result: Result<User.Tokens, Error>) in
            switch result {
            case .success(let tokens):
                self.storage.refreshToken = tokens.refreshToken
                self.storage.restToken = tokens.restToken
                completion(.success(()))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func confirm(email: String?, code: String, accountType: String?, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Encodable {
            let email: String?
            let userId: Int
            let accountType: String
            let confirmationCode: String
        }
        let params = Params(email: email, userId: storage.userId!, accountType: accountType ?? "main_account", confirmationCode: code)
        send(.post(json: params, api: .confirmEmail), completion: completion)
    }
    
    func confirmVerification(code: String, completion: @escaping (Result<Int, Error>) -> ()) {
        struct Params: Encodable {
            let userId: Int
            let accountType: String = "main_account"
            let confirmationCode: String
        }
        let params = Params(userId: storage.userId!, confirmationCode: code)
        struct Res: Decodable {
            let restToken: String
            let refreshToken: String
            let userId: Int
        }
        send(.post(json: params, api: .confirmEmail)) { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                self.storage.restToken = res.restToken
                self.storage.refreshToken = res.refreshToken
                completion(.success(res.userId))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func update(user: User.Request, completion: @escaping (Result<User, Error>) -> ()) {
        struct Wrap: Encodable {
            let properties: User.Request
        }
        send(.post(json: Wrap(properties: user), api: .updateUser), completion: completion)
    }
    
    func update(phone: User.Phone, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: phone, api: .updatePhone), completion: completion)
    }
    
    func getUpdateCode(phoneNumber: String, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Encodable {
            let phoneNumber: String
        }
        let params = Params(phoneNumber: phoneNumber)
        send(.post(json: params, api: .getUpdatePhoneCode), completion: completion)
    }
}

extension Session: CardsNetwork {
    func creteIntent(completion: @escaping (Result<String, Error>) -> ()) {
        struct Intent: Decodable {
            let clientSecret: String
        }
        send(.emptyPost(.card)) { (result: Result<Intent, Error>) in
            switch result {
            case .success(let intent):
                completion(.success(intent.clientSecret))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func add(card: CreditCard, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: card, api: .addCard), completion: completion)
    }
    
    func getCards(completion: @escaping (Result<[CreditCard], Error>) -> ()) {
        send(.emptyPost(.cards), completion: completion)
    }
    
    func setPrimary(card: CreditCard, completion: @escaping (Result<Void, Error>) -> ()) {
        let params: [String: Int] = ["id": card.cardId]
        send(.post(json: params, api: .setPrimary), completion: completion)
    }
    
    func delete(card: CreditCard, completion: @escaping (Result<Void, Error>) -> ()) {
        let params: [String: Int] = ["id": card.cardId]
        send(.post(json: params, api: .deleteCard), completion: completion)
    }
}

