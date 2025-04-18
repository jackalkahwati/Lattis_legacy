//
//  Session.swift
//  OvalExample
//
//  Created by Ravil Khusainov on 7/10/18.
//  Copyright © 2018 Lattis inc. All rights reserved.
//

import Foundation

open class Session {
    public static var contentLanguage: String = "en"
    public static var userAgent: String = "lattis"
    public static let shared = Session(storage: UserDefaults.standard)
    // UserDefauilts in not recommended to use in this case.
    // Please replace it with something more secure like Keychain
    public var storage: SensetiveStorage
    public var debugLogs: Bool = true
    fileprivate let session: URLSession
    fileprivate let decoder = JSONDecoder()
    fileprivate let encoder = JSONEncoder()
    fileprivate let threadSafe: (@escaping () -> ()) -> () = { DispatchQueue.main.async(execute: $0) }
    
    public init(storage: SensetiveStorage = Session.shared.storage) {
        self.storage = storage
        let configuration: URLSessionConfiguration = .default
        configuration.httpAdditionalHeaders = ["Accept": "application/json", "Content-Language": Session.contentLanguage, "User-Agent": Session.userAgent]
        self.session = URLSession(configuration: configuration, delegate: nil, delegateQueue: nil)
        self.decoder.keyDecodingStrategy = .convertFromSnakeCase
        self.decoder.dateDecodingStrategy = .secondsSince1970
        self.encoder.keyEncodingStrategy = .convertToSnakeCase
        self.encoder.outputFormatting = .prettyPrinted
        self.encoder.dateEncodingStrategy = .secondsSince1970
    }
    
    public func logout() {
        storage.refreshToken = nil
        storage.restToken = nil
        storage.userId = nil
    }
    
    @discardableResult public func download(by url: URL, completion: @escaping (Result<Data, Error>) -> ()) -> URLSessionTask {
        let task = session.dataTask(with: url) { (d, r, e) in
            if let error = e {
                return self.threadSafe {
                    completion(.failure(error))
                }
            } else if let data = d {
                return self.threadSafe {
                    completion(.success(data))
                }
            }
        }
        task.resume()
        return task
    }
    
    @discardableResult public func send<A>(_ request: Request<A>, envelope: Bool = true, completion: @escaping (Result<Void, Error>) -> ()) -> URLSessionTask? {
        let empty: (Empty) -> () = {_ in
            completion(.success(()))
        }
        return send(request, envelope: envelope, successData: empty, success: {
            completion(.success(()))
        }, fail: {
            completion(.failure($0))
        })
    }
    
    @discardableResult public func send<A, B: Decodable>(_ request: Request<A>, envelope: Bool = true, completion: @escaping (Result<B, Error>) -> ()) -> URLSessionTask? {
        return send(request, envelope: envelope, successData: {
            completion(.success($0))
        }, success: nil, fail: {
            completion(.failure($0))
        })
    }
    
    @discardableResult private func send<A, B: Decodable>(_ request: Request<A>, envelope: Bool, successData: @escaping (B) -> (), success: (() -> ())?, fail: @escaping (Swift.Error) -> ()) -> URLSessionTask?  {
        let handle: (ServerError, Data?) -> () = { error, data in
            if error.code == 412 {
                self.refreshToken(for: request, completion: { (err) in
                    if err != nil {
                        fail(error.error(request.api, data: data))
                    } else {
                        self.send(request, envelope: envelope, successData: successData, success: success, fail: fail)
                    }
                })
            } else {
                self.threadSafe {
                    fail(error.error(request.api, data: data))
                }
            }
        }
        if request.dateAsTimestamp {
            encoder.dateEncodingStrategy = .secondsSince1970
        } else {
            encoder.dateEncodingStrategy = .iso8601
        }
        do {
            let urlRequest = try transform(request)
            let task = session.dataTask(with: urlRequest) { (d, r, e) in
                if let error = e {
                    self.threadSafe {
                        fail(error)
                    }
                    self.consoleLog(request: request, response: r, received: d, sent: urlRequest.httpBody, error: e)
                    return
                }
                guard let data = d else {
                    let error = request.api.error(code: .emptyData)
                    self.threadSafe {
                        fail(error)
                    }
                    self.consoleLog(request: request, response: r, received: d, sent: urlRequest.httpBody, error: error)
                    return
                }
                do {
                    var err: Swift.Error? = nil
                    if request.dateAsTimestamp {
                        self.decoder.dateDecodingStrategy = .secondsSince1970
                    } else {
                        self.decoder.dateDecodingStrategy = .iso8601
                    }
                    if envelope {
                        let response = try self.decoder.decode(Response<B>.self, from: data)
                        self.threadSafe {
                            switch response {
                            case .result(let result):
                                successData(result)
                            case .error(let error):
                                handle(error, data)
                                err = error
                            case .none(let code):
                                if let success = success, code/200 == 1 {
                                    success()
                                } else {
                                    err = request.api.error(code: .unexpectedResponse)
                                    fail(err!)
                                }
                            }
                        }
                    } else {
                        let resonse = try self.decoder.decode(B.self, from: data)
                        self.threadSafe {
                            successData(resonse)
                        }
                    }
                    self.consoleLog(request: request, response: r, received: data, sent: urlRequest.httpBody, error: err)
                } catch {
                    self.threadSafe {
                        fail(error)
                    }
                    self.consoleLog(request: request, response: r, received: data, sent: urlRequest.httpBody, error: error)
                }
            }
            task.resume()
            return task
        } catch {
            threadSafe {
                fail(error)
            }
            consoleLog(request: request, error: error)
            return nil
        }
    }
    
    private func transform<A>(_ request: Request<A>) throws -> URLRequest {
        var urlRequest = URLRequest(url: request.api.url)
        urlRequest.aplyHTTPHeaders(from: request, with: storage.restToken)
        urlRequest.httpMethod = request.method
        switch request.content {
        case .json(let params):
            let data = try encoder.encode(params)
            urlRequest.httpBody = data
        case .multipart(let multipart):
            urlRequest.httpBody = multipart.body
        default:
            break
        }
        return urlRequest
    }
    
    private func refreshToken<A>(for request: Request<A>, completion: @escaping (Swift.Error?) -> ()) {
        guard let userId = storage.userId, let token = storage.refreshToken else {
            return completion(request.api.error(code: .refreshFailed(storage.userId, storage.refreshToken)))
        }
        send(.post(json: TokenRefreshParams(userId: userId, refreshToken: token), api: .refreshToken)) { (result: Result<Token, Error>) in
            switch result {
            case .success(let token):
                self.storage.restToken = token.restToken
                self.storage.refreshToken = token.refreshToken
                completion(nil)
            case .failure(let error):
                completion(error)
            }
        }
    }
    
    private func consoleLog<A>(request: Request<A>, response: URLResponse? = nil, received: Data? = nil, sent: Data? = nil, error: Swift.Error? = nil) {
        guard debugLogs else { return }
        if let data = sent, let str = String(data: data, encoding: .utf8) {
            print("params:", str)
        }
        print(request.method, request.api.url)
        if let resp = response as? HTTPURLResponse {
            print("status code:", resp.statusCode)
        }
        if let data = received, let str = String(prettyPrint: data) {
            print("response body:", str)
        } else if let data = received, let str = String(data: data, encoding: .utf8) {
            print("response body:", str)
        }
        if let error = error {
            print("error:", error)
        }
        print("\n")
    }
}

fileprivate extension URLRequest {
    mutating func aplyHTTPHeaders<A>(from request: Request<A>, with token: String?) {
        setValue("application/json", forHTTPHeaderField: "Accept")
        setValue("application/json", forHTTPHeaderField: "Content-Type")
        for (key, value) in request.headers {
            setValue(value, forHTTPHeaderField: key)
        }
        if let token = token {
            setValue(token, forHTTPHeaderField: "authorization")
        }
    }
}

extension String {
    init?(prettyPrint: Data) {
        if let json = try? JSONSerialization.jsonObject(with: prettyPrint, options: .mutableContainers) {
            if let prettyPrintedData = try? JSONSerialization.data(withJSONObject: json, options: .prettyPrinted) {
                self.init(data: prettyPrintedData, encoding: .utf8)
                return
            }
        }
        return nil
    }
}
