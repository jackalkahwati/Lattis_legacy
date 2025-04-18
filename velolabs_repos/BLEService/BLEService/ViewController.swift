//
//  ViewController.swift
//  BLEService
//
//  Created by Ravil Khusainov on 17/12/2016.
//  Copyright © 2016 Lattis. All rights reserved.
//

import UIKit
import SwiftyTimer

class ViewController: UIViewController {
    @IBOutlet weak var tableView: UITableView!
    var locks: [Peripheral] = []
    override func viewDidLoad() {
        super.viewDidLoad()
        
        BLEService.shared.subscribe(delegate: self)
        BLEService.shared.startScan()
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        BLEService.shared.unsubsribe(self)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let update = segue.destination as? FWUpdateViewController {
            update.per = sender as! Peripheral
        }
        if let controller = segue.destination as? LockViewController {
            controller.lock = sender as! Peripheral
        }
    }
}

extension ViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return locks.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! Cell
        cell.label.text = locks[indexPath.row].name
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if UserDefaults.standard.value(forKey: "userId") == nil {
            let alert = UIAlertController(title: "Warning!", message: "Please Log In first", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "LogIN", style: .default, handler: { (_) in
                self.performSegue(withIdentifier: "login", sender: nil)
            }))
            present(alert, animated: true, completion: nil)
        } else {
            performSegue(withIdentifier: "lock", sender: locks[indexPath.row])
        }
        
//        performSegue(withIdentifier: "update", sender: locks[indexPath.row])
    }
}

extension ViewController: BLEServiceDelegate {
    func service(_ service: BLEService, didRefresh peripherals: [Peripheral]) {
        self.locks = peripherals
        self.tableView.reloadData()
    }
}


class Cell: UITableViewCell {
    @IBOutlet weak var label: UILabel!
    
}
