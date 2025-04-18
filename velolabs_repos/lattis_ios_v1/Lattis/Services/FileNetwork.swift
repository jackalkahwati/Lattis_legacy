//
//  FilesNetwork.swift
//  Lattis
//
//  Created by Ravil Khusainov on 9/9/18.
//  Copyright © 2018 Velo Labs. All rights reserved.
//

import Oval

enum UploadType: String {
    case parking, maintenance, bike
}

fileprivate extension API {
    static func misc(path: String) -> API {
        return .init(path: "misc/" + path)
    }
    static func upload(type: UploadType) -> API {
        return misc(path: "upload?type=" + type.rawValue )
    }
}

protocol FileNetwork {
    func upload(data: Data, for type: UploadType, completion: @escaping (Result<URL, Error>) -> ())
}

extension Session: FileNetwork {
    func upload(data: Data, for type: UploadType, completion: @escaping (Result<URL, Error>) -> ()) {
        struct Wrap: Decodable {
            let uploadedUrl: URL
        }
        send(.post(multipart: .init(data: data), api: .upload(type: type))) { (result: Result<Wrap, Error>) in
            switch result {
            case .success(let wrap):
                completion(.success(wrap.uploadedUrl))
            case .failure(let e):
                completion(.failure(e))
            }
        }
    }
}
