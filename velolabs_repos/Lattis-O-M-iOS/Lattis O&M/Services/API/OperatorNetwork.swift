//
//  OperatorNetwork.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 03/05/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Oval

protocol OperatorNetwork {
    func login(with credentials: Operator.LogIn, completion: @escaping (Result<Int, Error>) -> ())
    func getOperators(for fleet: Fleet, completion: @escaping (Result<[Operator], Error>) -> ())
    
    func getTokens(userId: Int, password: String, completion: @escaping (Result<Void, Error>) -> ())
    func signInCode(completion: @escaping (Result<Void, Error>) -> ())
    func confirm(signIn code: String, completion: @escaping (Result<Void, Error>) -> ())
}

fileprivate extension API {
    static func operators(_ path: String) -> API {
        return .init(path: "operator/" + path)
    }
    static let login = operators("login")
    static let getOperators = operators("get-operator")
    
    static func users(_ path: String) -> API {
        return .init(path: "users/" + path)
    }
    static let tokens = users("new-tokens")
    static let code = users("sign-in-code")
    static let confirm = users("confirm-user-code")
}

extension Session: OperatorNetwork {
    func login(with credentials: Operator.LogIn, completion: @escaping (Result<Int, Error>) -> ()) {
        struct Wrap: Decodable {
            let `operator`: Operator
            
            struct Operator: Decodable {
                let operatorId: Int
                let restToken: String
                let refreshToken: String
            }
        }
        send(.post(json: credentials, api: .login)) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                let oper = wrap.operator
                self.storage.restToken = oper.restToken
                self.storage.refreshToken = oper.refreshToken
                self.storage.userId = oper.operatorId
                completion(.success(oper.operatorId))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    func getOperators(for fleet: Fleet, completion: @escaping (Result<[Operator], Error>) -> ()) {
        send(.post(json: fleet, api: .getOperators), completion: completion)
    }
    
    func getTokens(userId: Int, password: String, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Res: Decodable {
            let restToken: String
            let refreshToken: String
        }
        struct Params: Encodable {
            let userId: Int
            let password: String
        }
        let params = Params(userId: userId, password: password)
        send(.post(json: params, api: .tokens)) { (result: Result<Res, Error>) in
            switch result {
            case .success(let res):
                self.storage.restToken = res.restToken
                self.storage.refreshToken = res.refreshToken
                self.storage.userId = userId
                completion(.success(()))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
    
    func signInCode(completion: @escaping (Result<Void, Error>) -> ()) {
        let params: [String: Int] = ["user_id": storage.userId!]
        send(.post(json: params, api: .code), completion: completion)
    }
    
    func confirm(signIn code: String, completion: @escaping (Result<Void, Error>) -> ()) {
        struct Params: Encodable {
            let userId: Int
            let confirmationCode: String
        }
        let params =  Params(userId: storage.userId!, confirmationCode: code)
        send(.post(json: params, api: .confirm), completion: completion)
    }
}

