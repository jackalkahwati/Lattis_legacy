//
//  FWUpdateViewController.swift
//  BLEService
//
//  Created by Ravil Khusainov on 5/27/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class FWUpdateViewController: UIViewController {
    var per: Peripheral!
    override func viewDidLoad() {
        super.viewDidLoad()
        BLEService.shared.connect(per)
        do {
            let original = try Data(contentsOf: Bundle.main.url(forResource: "bad", withExtension: "BIN")!)
            var bytes = [UInt8](original)
            var result: [UInt8] = []
            
            while bytes.count > 0 {
                let limit = bytes.count > 128 ? 127 : bytes.count - 1
                var array: [UInt8] = [0,0,0,0] + Array(bytes[0...limit])
                if bytes.count >= 128 {
                    bytes = Array(bytes[(limit + 1)...(bytes.count - 1)])
                } else {
                    bytes.removeAll()
                }
                
                while array.count < 132 {
                    array.append(0xf)
                }
                result += array
            }
            
            per.update(firmware: result, progress: {print($0)})
            
        } catch {
            
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}


