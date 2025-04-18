//
//  Oval+User.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/7/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import Oval

protocol UserNetwork {
    func user(_ userId: Int?, completion: @escaping (Result<User, Error>) -> ())
    func update(user: User, completion: @escaping (Result<User, Error>) -> ())
    func getUpdatePasswordCode(completion: @escaping (Result<Void, Error>) -> ())
    func update(password: String, with confirmationCode: String, completion: @escaping (Result<Void, Error>) -> ())
    func getUpdateCode(for phoneNumber: String, completion: @escaping (Result<Void, Error>) -> ())
    func update(phoneNumber: String, with confirmationCode: String, completion: @escaping (Result<Void, Error>) -> ())
    func getUpdateCode(email: String, completion: @escaping (Result<Void, Error>) -> ())
    func update(email: String, with confirmationCode: String, completion: @escaping (Result<Void, Error>) -> ())
    func delete(completion: @escaping (Result<Void, Error>) -> ())
    func login(user: User.Credentials, completion: @escaping (Result<(Int, Bool), Error>) -> ())
    func forgotPassword(phone: String, completion: @escaping (Result<Void, Error>) -> ())
    func confirm(forgot code: String, phone: String, password: String, completion: @escaping (Result<Void, Error>) -> ())
    func confirm(signIn code: String, completion: @escaping (Result<Void, Error>) -> ())
    func getTermsAndConditions(completion: @escaping (Result<(String, String), Error>) -> ())
    func acceptTermsAndConditions(completion: @escaping (Result<Void, Error>) -> ())
    func checkTerms(completion: @escaping (Result<Bool, Error>) -> ())
    func getTokens(userId: Int, password: String, completion: @escaping (Result<Void, Error>) -> ())
    func signInCode(completion: @escaping (Result<Void, Error>) -> ())
}

extension Session: UserNetwork {
    func user(_ userId: Int?, completion: @escaping (Result<User, Error>) -> ()) {
        struct Res: Codable {
            let user: User
        }
        send(.post(json: ["user_id": userId], api: .users(.getUser)), completion: { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                completion(.success(res.user))
            case .failure(let e):
                completion(.failure(e))
            }
        })
    }
    
    func update(user: User, completion: @escaping (Result<User, Error>) -> ()) {
        struct Res: Codable {
            let user: User
        }
        send(.post(json: ["properties": user], api: .users(.updateUser)), completion: { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                completion(.success(res.user))
            case .failure(let e):
                completion(.failure(e))
            }
        })
    }
    
    func getUpdatePasswordCode(completion: @escaping (Result<Void, Error>) -> ()) {
        send(.get(.users(.updatePasswordCode)), completion: completion)
    }
    
    func update(password: String, with confirmationCode: String, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Codable {
            let password: String
            let confirmationCode: String
        }
        let params = Params(password: password, confirmationCode: confirmationCode)
        send(.post(json: params, api: .users(.updatePassword)), completion: completion)
    }
    
    func getUpdateCode(for phoneNumber: String, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: ["phone_number": phoneNumber], api: .users(.updatePhoneNumberCode)), completion: completion)
    }
    
    func update(phoneNumber: String, with confirmationCode: String, completion: @escaping (Result<Void, Error>) -> ()) {
        let params: [String: String] = ["phone_number": phoneNumber, "confirmation_code": confirmationCode]
        send(.post(json: params, api: .users(.updatePhoneNumber)), completion: completion)
    }
    
    func getUpdateCode(email: String, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: ["email": email], api: .users(.updateEmailCode)), completion: completion)
    }
    
    func update(email: String, with confirmationCode: String, completion: @escaping (Result<Void, Error>) -> ()) {
        let params: [String: String] = ["email": email, "confirmation_code": confirmationCode]
        send(.post(json: params, api: .users(.updateEmail)), completion: completion)
    }
    
    func delete(completion: @escaping (Result<Void, Error>) -> ()) {
        send(.get(.users(.deleteAccount)), completion: completion)
    }
    
    func login(user: User.Credentials, completion: @escaping (Result<(Int, Bool), Error>) -> ()) {
        struct Res: Codable {
            let userId: Int
            let verified: Bool
        }
        send(.post(json: user, api: .users(.registration)), completion: { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                completion(.success((res.userId, res.verified)))
            case .failure(let e):
                completion(.failure(e))
            }
        })
    }
    
    func forgotPassword(phone: String, completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: ["phone_number": phone], api: .users(.forgotPasswordCode)), completion: completion)
    }
    
    func confirm(forgot code: String, phone: String, password: String, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Encodable {
            let phoneNumber: String
            let confirmationCode: String
            let password: String
        }
        let params = Params(phoneNumber: phone, confirmationCode: code, password: password)
        send(.post(json: params, api: .users(.confirmForgotPasswordCode)), completion: completion)
    }
    
    func confirm(signIn code: String, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Codable {
            let confirmationCode: String
            let userId: Int
        }
        struct Res: Codable {
            let restToken: String
            let refreshToken: String
        }
        let params = Params(confirmationCode: code, userId: storage.userId!)
        send(.post(json: params, api: .users(.confirmUserCode)), completion: { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                self.storage.restToken = res.restToken
                self.storage.refreshToken = res.refreshToken
                completion(.success(()))
            case .failure(let e):
                completion(.failure(e))
            }
        })
    }
    
    func getTermsAndConditions(completion: @escaping (Result<(String, String), Error>) -> ()) {
        struct Res: Codable {
            let termsAndConditions: Terms
            struct Terms: Codable {
                let version: String
                let terms: String
            }
        }
        send(.get(.users(.termsAndConditions)), completion: { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                completion(.success((res.termsAndConditions.version, res.termsAndConditions.terms)))
            case .failure(let e):
                completion(.failure(e))
            }
        })
    }
    
    func acceptTermsAndConditions(completion: @escaping (Result<Void, Error>) -> ()) {
        send(.post(json: ["did_accept": true], api: .users(.acceptTermsAndConditions)), completion: completion)
    }
    
    func checkTerms(completion: @escaping (Result<Bool, Error>) -> ()) {
        struct Res: Codable {
            let hasAccepted: Bool
        }
        send(.get(.users(.checkAcceptedTermsAndConditions)), completion: { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                completion(.success(res.hasAccepted))
            case .failure:
                completion(.success(false))
            }
        })
    }
    
    func getTokens(userId: Int, password: String, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Codable {
            let userId: Int
            let password: String
        }
        struct Res: Codable {
            let restToken: String
            let refreshToken: String
        }
        send(.post(json: Params(userId: userId, password: password), api: .users(.newTokens)), completion: { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                self.storage.restToken = res.restToken
                self.storage.refreshToken = res.refreshToken
                completion(.success(()))
            case .failure(let e):
                completion(.failure(e))
            }
        })
    }
    
    func signInCode(completion: @escaping (Result<Void, Error>) -> ()) {
        guard let userId = storage.userId else { return completion(.failure(SessionError.Code.unauthorized)) }
        send(.post(json: ["user_id": userId], api: .users(.signInCode)), completion: completion)
    }
}

