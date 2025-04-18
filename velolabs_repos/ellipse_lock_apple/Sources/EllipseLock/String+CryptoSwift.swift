//
//  String+CryptoSwift.swift
//  LattisSDK
//
//  Created by Ravil Khusainov on 8/2/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import CommonCrypto
import Foundation

extension String {
    var challengeKeyValue: String {
        var value = self.md5()
        while value.count < 64 {
            value.append("f")
        }
        return value.lowercased()
    }
    
    public var dataValue: Data? {
        guard let bytes = bytesArray else { return nil }
        return Data(bytes)
    }
    
    public var bytesArray: [UInt8]? {
        if count % 2 == 1 {
            print("cannot convert \(self) to bytes string. Odd number of digits.")
            return nil
        }
        
        var bytes:[UInt8] = [UInt8]()
        var index:Int = 0
        while index < count {
            let startIndex = self.index(self.startIndex, offsetBy: index)
            let endIndex = self.index(startIndex, offsetBy: 2)
            let subString = self[startIndex..<endIndex]
            if let value = UInt8(subString, radix: 16) {
                bytes.append(value)
            }
            
            index += 2
        }
        
        return bytes
    }
    
    func md5() -> String {
        if let strData = data(using: String.Encoding.utf8) {
               /// #define CC_MD5_DIGEST_LENGTH    16          /* digest length in bytes */
               /// Creates an array of unsigned 8 bit integers that contains 16 zeros
               var digest = [UInt8](repeating: 0, count:Int(CC_MD5_DIGEST_LENGTH))
        
               /// CC_MD5 performs digest calculation and places the result in the caller-supplied buffer for digest (md)
               /// Calls the given closure with a pointer to the underlying unsafe bytes of the strData’s contiguous storage.
               _ = strData.withUnsafeBytes {
                   // CommonCrypto
                   // extern unsigned char *CC_MD5(const void *data, CC_LONG len, unsigned char *md) --|
                   // OpenSSL                                                                          |
                   // unsigned char *MD5(const unsigned char *d, size_t n, unsigned char *md)        <-|
                   CC_MD5($0.baseAddress, UInt32(strData.count), &digest)
               }
        
        
               var md5String = ""
               /// Unpack each byte in the digest array and add them to the md5String
               for byte in digest {
                   md5String += String(format:"%02x", UInt8(byte))
               }

               return md5String
        }
        return ""
    }
}

public extension Data {
    var ellipseVersion: String? {
        return version(versionIndex: 9, reversionIndex: 11)
    }
    
    var bootLoaderVersion: String? {
        return version(versionIndex: 5, reversionIndex: 7)
    }
    
    private func version(versionIndex: Int, reversionIndex: Int) -> String? {
        let values = [UInt8](self)
        
        guard values.count > versionIndex else { return nil }
        var version = "\(values[versionIndex])"
        
        guard values.count > reversionIndex else { return version }
        version += String(format: ".%02d", values[reversionIndex])
        
        return version
    }
    
    var hex: String {
        return map { String(format: "%02x", $0) }.joined(separator: "")
    }
    
    func sha256() -> Data {
        var hash = [UInt8](repeating: 0,  count: Int(CC_SHA256_DIGEST_LENGTH))
        self.withUnsafeBytes {
            _ = CC_SHA256($0.baseAddress, CC_LONG(self.count), &hash)
        }
        return Data(hash)
    }
}
