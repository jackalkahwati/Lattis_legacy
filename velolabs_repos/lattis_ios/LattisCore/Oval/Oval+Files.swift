//
//  Oval+Files.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 07/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import OvalAPI

fileprivate extension API {
    static func misc(path: String) -> API {
        return .init(path: "misc/" + path)
    }
    static func upload(type: UploadType) -> API {
        return misc(path: "upload?type=" + type.rawValue )
    }
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
