//
//  Oval+Apps.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 11.01.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import OvalAPI
import Model

fileprivate extension API {
    static let info = API(path: "apps/info")
}

extension Session: AppsAPI {
    func fetchInfo(completion: @escaping (Result<AppInfo, Error>) -> ()) {
        send(.get(.info), completion: completion)
    }
}
