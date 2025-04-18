//
//  Converter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 4/13/18.
//  Copyright © 2018 Velo Labs. All rights reserved.
//

import Foundation

func currency(_ code: String) -> (NumberFormatter) -> () {
    return {
        $0.numberStyle = .currency
        $0.currencyCode = code
    }
}

struct Converter {
    struct Date {
        static let iso8601: DateFormatter = {
            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
            formatter.locale = Locale(identifier: "en_US_POSIX")
            return formatter
        }()
        
        static let shared = DateFormatter()
    }
    
    struct Number {
        static let shared = NumberFormatter()
    }
}

extension Double {
    func converted(_ transform: (NumberFormatter) -> ()) -> String? {
        let formatter = Converter.Number.shared
        transform(formatter)
        return formatter.string(from: NSNumber(value: self))
    }
}
