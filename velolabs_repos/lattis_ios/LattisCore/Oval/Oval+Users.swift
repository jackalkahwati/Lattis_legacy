//
//  Oval+Users.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 29/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation
import OvalAPI

fileprivate extension API {
    enum UsersPath: String {
        case registration
        case tokens = "new-tokens"
        case status = "get-current-status"
        case refresh = "get-user"
        case update = "update-user"
        case fleet = "add-private-account"
        case verification = "email-verification-code"
        case confirmation = "confirm-email-verification-code"
        case email = "update-email"
        case emailCode = "update-email-code"
        case phone = "update-phone-number"
        case phoneCode = "update-phone-number-code"
        case passwordCode = "update-password-code"
        case restorePassword = "forgot-password"
        case confirmNewPassword = "confirm-forgot-password"
        case changePassword = "change-password"
        case deleteAccount = "delete"
    }
    
    static func users(_ path: UsersPath) -> API {
        return .init(path: "users/" + path.rawValue)
    }
}

fileprivate var verificationUserId: Int?

extension Session: UserAPI {
    
    struct Empty: Codable {}
    
    private struct Confirmation: Encodable {
        let userId: Int
        let accountType: Account
        let email: String?
        let confirmationCode: String?
        
        enum Account: String, Encodable {
            case mainAccount = "main_account"
            case privateAccount = "private_account"
        }
    }
    
    private struct Tokens: Decodable {
        let restToken: String
        let refreshToken: String
    }
    
    func checkStatus(completion: @escaping (Result<Status.Info, Error>) -> ()) {
        send(.post(json: UIDevice.current, api: .users(.status)), completion: completion)
    }
    
    func refresh(completion: @escaping (Result<User.Update, Error>) -> ()) {
        send(.post(json: Empty(), api: .users(.refresh)), completion: completion)
    }
    
    func logIn(user: User.LogIn, completion: @escaping (Result<Bool, Error>) -> ()) {
        struct Wrap: Decodable {
            let userId: Int
            let verified: Bool
        }
        struct TokenRequest: Encodable {
            let userId: Int
            let password: String
        }
        send(.post(json: user, api: .users(.registration))) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                if wrap.verified {
                    self.storage.userId = wrap.userId
                    self.send(.post(json: TokenRequest(userId: wrap.userId, password: user.password), api: .users(.tokens))) { (r: Result<Tokens, Error>) in
                        switch r {
                        case .success(let token):
                            self.storage.restToken = token.restToken
                            self.storage.refreshToken = token.refreshToken
                            AppRouter.shared.loggedIn(userId: wrap.userId) {
                                completion(.success(true))
                            }
                        case .failure(let e):
                            completion(.failure(e))
                        }
                    }
                } else {
                    verificationUserId = wrap.userId
                    self.verify(email: user.email, code: nil) { (res) in
                        switch res {
                        case .success:
                            completion(.success(false))
                        case .failure(let error):
                            completion(.failure(error))
                        }
                    }
                }
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func addPrivateNetwork(email: String, code: String?, completion: @escaping (Result<[Fleet]?, Error>) -> ()) {
        let params: [String: String] = ["email": email]
        struct Wrap: Decodable {
            let lattisAccount: [Fleet]
            
            struct Fleet: Decodable {
                let email: String
            }
        }
        if let c = code {
            guard let userId = storage.userId else { return completion(.failure(SessionError.Code.unauthorized))}
            let params = Confirmation(userId: userId, accountType: .privateAccount, email: email, confirmationCode: c)
            send(.post(json: params, api: .users(.confirmation))) { (result: Result<Empty, Error>) in
                switch result {
                case .success:
                    self.refresh(completion: { (result) in
                        switch result {
                        case .success(let update):
                            completion(.success(update.fleets))
                        case .failure(let error):
                            completion(.failure(error))
                        }
                    })
                case .failure(let e):
                    completion(.failure(e))
                }
            }
        } else {
            send(.post(json: params, api: .users(.fleet))) { (result: Result<Wrap, Error>) in
                switch result {
                case .success(let wrap):
                    if wrap.lattisAccount.isEmpty {
                        completion(.failure(UserError.noFleetsToAdd))
                    } else {
                        completion(.success(nil))
                    }
                case .failure(let e):
                    completion(.failure(e))
                }
            }
        }
    }
    
    func update(user: User, completion: @escaping (Result<User, Error>) -> ()) {
        struct Wrap: Encodable {
            let properties: User
        }
        struct ResponseWrap: Decodable {
            let user: User
        }
        send(.post(json: Wrap(properties: user), api: .users(.update))) { (result: Result<ResponseWrap, Error>) in
            switch result {
            case .success(let wrap):
                completion(.success(wrap.user))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func update(email: String, code: String?, completion: @escaping (Result<User?, Error>) -> ()) {
        var params: [String: String] = ["email": email]
        if let c = code {
            params["confirmation_code"] = c
            send(.post(json: params, api: .users(.email))) { (result: Result<Void, Error>) in
                switch result {
                case .success:
                    self.refresh(completion: { (result) in
                        switch result {
                        case .success(let r):
                            completion(.success(r.user))
                        case .failure(let e):
                            completion(.failure(e))
                        }
                    })
                case .failure(let e):
                    completion(.failure(e))
                }
            }
        } else {
            send(.post(json: params, api: .users(.emailCode))) { (result: Result<Void, Error>) in
                switch result {
                case .success:
                    completion(.success(nil))
                case .failure(let e):
                    completion(.failure(e))
                }
            }
        }
    }
    
    func update(phone: String, code: String?, completion: @escaping (Result<User?, Error>) -> ()) {
        var params: [String: String] = ["phone_number": phone]
        if let c = code {
            params["confirmation_code"] = c
            send(.post(json: params, api: .users(.phone))) { (result: Result<Void, Error>) in
                switch result {
                case .success:
                    self.refresh(completion: { (ref) in
                        switch ref {
                        case .success(let res):
                            completion(.success(res.user))
                        case .failure(let e):
                            completion(.failure(e))
                        }
                    })
                case .failure(let e):
                    completion(.failure(e))
                }
            }
        } else {
            send(.post(json: params, api: .users(.phoneCode))) { (result: Result<Void, Error>) in
                switch result {
                case .success:
                    completion(.success(nil))
                case .failure(let e):
                    completion(.failure(e))
                }
            }
        }
    }
    
    func verify(email: String, code: String?, completion: @escaping (Result<Void, Error>) -> ()) {
        guard let userId = verificationUserId else {
            completion(.failure(UserError.noUserIdFoundForVerification))
            return
        }
        if let code = code {
            let params = Confirmation(userId: userId, accountType: .mainAccount, email: email, confirmationCode: code)
            send(.post(json: params, api: .users(.confirmation)), completion: { (result: Result<Tokens, Error>) in
                switch result {
                case .success(let token):
                    self.storage.userId = userId
                    self.storage.restToken = token.restToken
                    self.storage.refreshToken = token.refreshToken
                    AppRouter.shared.loggedIn(userId: userId) {
                        completion(.success(()))
                    }
                case .failure(let e):
                    completion(.failure(e))
                }
            })
        } else {
            let params = Confirmation(userId: userId, accountType: .mainAccount, email: email, confirmationCode: nil)
            send(.post(json: params, api: .users(.verification)), completion: completion)
        }
    }
    
    func restorePasswrd(email: String, code: String?, password: String?, completion: @escaping (Result<Void, Error>) -> ()) {
        if let code = code, let pass = password {
            send(.post(json: ["email": email, "confirmation_code": code, "password": pass], api: .users(.confirmNewPassword)), completion: completion)
        } else {
            send(.post(json: ["email": email], api: .users(.restorePassword)), completion: completion)
        }
    }
    
    func update(password: String, newPassword: String, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: ["password": password, "new_password": newPassword], api: .users(.changePassword)), completion: completion)
    }
    func deleteAccount(completion: @escaping (Result<User?, Error>) -> ()) {
        send(.put(Empty(), api: .users(.deleteAccount)), completion: completion)
    }
  
}

enum UserError: Error {
    case noFleetsToAdd
    case noUserIdFoundForVerification
}
