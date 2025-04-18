//
//  PrettyPrint.swift
//  
//
//  Created by Ravil Khusainov on 15.06.2021.
//

import Foundation


extension String {
    init?(json: Data) {
        guard let data = try? json.prettyJSON() else { return nil }
        self.init(data: data, encoding: .utf8)
    }
}

extension Data {
    func prettyJSON() throws -> Data {
        let json = try JSONSerialization.jsonObject(with: self, options: .mutableContainers)
        let options: JSONSerialization.WritingOptions
        if #available(macOS 10.15, *) {
            options = [.withoutEscapingSlashes, .prettyPrinted]
        } else {
            options = .prettyPrinted
        }
        return try JSONSerialization.data(withJSONObject: json, options: options)
    }
}
