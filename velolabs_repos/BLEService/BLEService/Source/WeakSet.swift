//
//  WeakSet.swift
//  BLEService
//
//  Created by Ravil Khusainov on 8/9/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

class WeakSet<T: AnyObject>: Sequence {
    private let storage = NSHashTable<T>.weakObjects()
    
    var count: Int {
        return storage.count
    }
    
    var isEmpty: Bool {
        return storage.count < 1
    }
    
    func insert(_ element: T) {
        guard contains(element) == false else { return }
        storage.add(element)
    }
    
    func contains(_ element: T) -> Bool {
        return storage.contains(element)
    }
    
    func remove(_ element: T) {
        storage.remove(element)
    }
    
    func makeIterator() -> AnyIterator<T> {
        let enumerator = storage.objectEnumerator()
        return AnyIterator<T> {
            return enumerator.nextObject() as? T
        }
    }
}


//class WeakSet<ObjectType: AnyObject>: Sequence {
//    
//    var count: Int {
//        return weakStorage.count
//    }
//    
//    private let weakStorage = NSHashTable<ObjectType>.weakObjects()
//    
//    func insert(object: ObjectType) {
//        weakStorage.add(object)
//    }
//    
//    func removeObject(object: ObjectType) {
//        weakStorage.remove(object)
//    }
//    
//    func removeAllObjects() {
//        weakStorage.removeAllObjects()
//    }
//    
//    func containsObject(object: ObjectType) -> Bool {
//        return weakStorage.contains(object)
//    }
//    
//    func generate() -> AnyIterator<ObjectType> {
//        let enumerator = weakStorage.objectEnumerator()
//        return AnyIterator {
//            return enumerator.nextObject() as! ObjectType?
//        }
//    }
//}
