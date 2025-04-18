//
//  CoreData+EllipseStorage.swift
//  Lattis
//
//  Created by Ravil Khusainov on 15/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import CoreData
import LattisSDK

extension CoreDataStack: EllipseStorage {
    func ellipse(with lockId: Int32) -> Ellipse? {
        return nil
    }
    
    func ellipse(with macId: String) -> Ellipse? {
        do {
            if let lock: CDLock = try read(with: NSPredicate(format: "macId = %@", macId)).first {
                return Ellipse(lock)
            }
            return nil
        } catch {
            print(error)
            return nil
        }
    }
    
    func save(ellipse: Ellipse) {
        write(completion: { (context) in
            var lock = try? CDLock.find(in: context, with: NSPredicate(format: "macId = %@", ellipse.macId!))
            if lock == nil {
                lock = CDLock.create(in: context)
            }
            lock?.fill(with: ellipse)
        }, fail: { error in
            print(error)
        })
    }
    
    func delete(ellipse: Ellipse) {
        write(completion: { (context) in
            do {
                if let lock = try CDLock.find(in: context, with: NSPredicate(format: "macId = %@", ellipse.macId!)) {
                    context.delete(lock)
                }
            } catch {
                print(error)
            }
        }, fail: { error in
            print(error)
        })
    }
}
