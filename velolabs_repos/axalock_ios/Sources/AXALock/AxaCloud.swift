
//
//  AXAKey.swift
//  AXALock
//
//  Created by Ravil Khusainov on 11.02.2020.
//

import Foundation


public struct AxaCloud {
    let encoder = JSONEncoder()
    let decoder = JSONDecoder()
    let endpoint = "https://dev-dot-keysafe-cloud.appspot.com/api/v1/locks"
    let apiKey = "79eb4d57e0714d95a590d04479490b42"
    
    func requestKey(for lock: AxaBLE.Lock, completion: @escaping (Error?) -> ()) {
        getLock(by: lock.id) { (result) in
            switch result {
            case .success(let id):
                self.getKeys(by: id) { (result) in
                    switch result {
                    case .success(let key):
                        lock.ekey = key.ekey.components(separatedBy: "-")
                        lock.passkey = key.passkey.components(separatedBy: "-")
                        completion(nil)
                    case .failure(let error):
                        completion(error)
                    }
                }
            case .failure(let error):
                completion(error)
            }
        }
    }
    
    public func claim(lock: Claim, completion: @escaping (Result<Void, Error>) -> ()) {
        var request = self.request(for: .home)
        struct Lock: Codable {
            let id: UInt
        }
        struct Response: Codable {
            let result: Lock
        }
        request.httpMethod = "POST"
        do {
            request.httpBody = try encoder.encode(lock)
        } catch {
            return completion(.failure(error))
        }
        URLSession.shared.dataTask(with: request) { (d, r, e) in
            if let error = e {
                return completion(.failure(error))
            }
            guard let data = d else { return completion(.failure(CloudError.emptyData)) }
            do {
                let _ = try self.decoder.decode(Response.self, from: data)
                completion(.success(()))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }
    
    fileprivate func getLock(by uid: String, completion: @escaping (Result<String, Error>) -> ()) {
        var request = self.request(for: .lock(uid))
        request.httpMethod = "GET"
        struct Lock: Codable {
            let id: UInt
        }
        struct Response: Codable {
            let result: [Lock]
        }
        URLSession.shared.dataTask(with: request) { (d, r, e) in
            if let error = e {
                return completion(.failure(error))
            }
            guard let data = d else { return completion(.failure(CloudError.emptyData)) }
            do {
                let resp = try self.decoder.decode(Response.self, from: data)
                guard let lock = resp.result.first else { return completion(.failure(CloudError.noLockFound)) }
                completion(.success(String(lock.id)))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }
    
    fileprivate func getKeys(by id: String, completion: @escaping (Result<Key, Error>) -> ()) {
        var request = self.request(for: .slots(id))
        struct Response: Codable {
            let result: Key
        }
        do {
            request.httpBody = try encoder.encode(Request())
        } catch {
            return completion(.failure(error))
        }
        request.httpMethod = "PUT"
        URLSession.shared.dataTask(with: request) { d, r, e in
            if let error = e {
                return completion(.failure(error))
            }
            guard let data = d else { return completion(.failure(CloudError.emptyData)) }
            do {
                let resp = try self.decoder.decode(Response.self, from: data)
                completion(.success(resp.result))
            } catch {
                if let str = String(data: d!, encoding: .utf8) {
                    print(str)
                }
                completion(.failure(error))
            }
        }.resume()
    }
    
    fileprivate func request(for route: Route) -> URLRequest {
        var request = URLRequest(url: URL(string: endpoint + route.path)!)
        request.addValue("KSC-TBT; gzip", forHTTPHeaderField: "User-Agent")
        request.addValue("gzip,deflate", forHTTPHeaderField: "Accept-Encoding")
        request.addValue("application/json, text/javascript", forHTTPHeaderField: "Accept")
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue(apiKey, forHTTPHeaderField: "X-Api-Key")
        return request
    }
}

extension AxaCloud {
    public struct Claim: Codable {
        public let lock_uid: String
        public let claim_code: String
        
        public init(lock_uid: String, claim_code: String) {
            self.claim_code = claim_code
            self.lock_uid = lock_uid
        }
    }
    
    struct Key: Codable {
        let passkey: String
        let ekey: String
    }

    enum KeyType: String, Codable {
        case code
        case otp
        case pin
    }

    enum Route {
        case home
        case lock(String)
        case slots(String)
        
        var path: String {
            switch self {
            case .home:
                return ""
            case .lock(let uid):
                return "?lock_uids=\(uid)"
            case .slots(let id):
                return "/\(id)/slots/1"
            }
        }
    }
}


public enum CloudError: Error {
    case emptyData
    case noLockFound
}

extension AxaCloud {
    struct Request: Encodable {
        let hours: Int = 0
        let passkey_type: KeyType = .otp
        let nr_of_passkeys: Int = 40
        let segmented: Bool = true
    }
}


