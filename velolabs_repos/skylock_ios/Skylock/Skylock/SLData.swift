//
//  SLData.swift
//  Ellipse
//
//  Created by Andre Green on 10/5/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import Foundation

extension NSData {
    func UInt8Array() -> [UInt8] {
        let values:[UInt8] = Array(
            UnsafeBufferPointer(start: self.bytes.assumingMemoryBound(to: UInt8.self),
                                count: self.length)
        )
        
        return values
    }
    
    func Int8Array() -> [Int8] {
        let values:[Int8] = Array(
            UnsafeBufferPointer(start: self.bytes.assumingMemoryBound(to: Int8.self),
                                count: self.length)
        )
        
        return values
    }
}
