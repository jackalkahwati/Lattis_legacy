//
//  RootViewController.swift
//  Lattis SDK Demo
//
//  Created by Ravil Khusainov on 8/2/18.
//  Copyright © 2018 Lattis Inc. All rights reserved.
//

import UIKit
import LattisSDK
import Oval
import GradientCircularProgress
import CryptoSwift

class RootViewController: UITableViewController {
    var locks: [Ellipse] = []
    var connected: Set<Ellipse> = []
    let manager = EllipseManager.shared
    
    fileprivate var lockedEllipse: Ellipse?
    fileprivate var progressView: GradientCircularProgress?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        manager.restoringStrategy = .disconnect
        manager.network = Session.shared
        manager.scan(with: self)
                
        navigationItem.rightBarButtonItem = .init(title: "Token", style: .plain, target: self, action: #selector(token))
    }
    
    @objc func token() {
//        Session.shared.send(.get(.init(endpoint: "http://192.168.2.40:3001/api", path: "trips/clean")), success: {}, fail: {print($0)})
        Session.shared.send(.get(.init(endpoint: "http://192.168.2.40:3001/api", path: "locks/get-keys"))) { _ in }
//        Session.shared.send(.get(.init(endpoint: "http://10.0.0.232:3001/api", path: "locks/get-keys")), success: {}, fail: {print($0)})
        
//        Session.shared.send(.post(json: ["fleetId":120], api: .init(endpoint: "https://lattisappv2.lattisapi.io/api", path: "fleet/token")), success: {}, fail: {_ in})
        let controller = storyboard?.instantiateViewController(withIdentifier: "settings") as! SettingsViewController
        present(controller, animated: true, completion: nil)
        
//        let key = "yKDnoDGzxL".challengeKeyValue
//        print(key)

    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return locks.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! EllipseTableCell
        cell.ellipse = locks[indexPath.row]
        return cell
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let ellipse = locks[indexPath.row]
        let dialog = UIAlertController(title: "Action", message: ellipse.name, preferredStyle: .actionSheet)
        if case .paired = ellipse.connection {
            dialog.addAction(.init(title: "Open", style: .default, handler: { _ in
                let controller = self.storyboard?.instantiateViewController(withIdentifier: "ellipse") as! EllipseViewController
                controller.ellipse = ellipse
                self.navigationController?.pushViewController(controller, animated: true)
                ellipse.connect(handler: self)
            }))
            dialog.addAction(.init(title: "Disconnect", style: .default, handler: { _ in
                ellipse.disconnect()
            }))
            dialog.addAction(.init(title: "Reset", style: .default, handler: { (_) in
                ellipse.factoryReset()
            }))
        } else {
            dialog.addAction(.init(title: "Connect", style: .default, handler: { _ in
                ellipse.connect(handler: self)
                self.progress(text: "Connecting...")
            }))
            dialog.addAction(.init(title: "Flash LED", style: .default, handler: { _ in
                self.progress(text: "Flashing LED")
                ellipse.flashLED(completion: {_ in self.hideProgress()})
            }))
            dialog.addAction(.init(title: "Disable PIN sensor", style: .default, handler: { (_) in
                ellipse.isCapTouchEnabled = false
            }))
            dialog.addAction(.init(title: "Enable PIN sensor", style: .default, handler: { (_) in
                ellipse.isCapTouchEnabled = true
            }))
        }
        dialog.addAction(.init(title: "Cancel", style: .cancel, handler: nil))
        present(dialog, animated: true, completion: nil)
    }
    
    fileprivate func progress(text: String) {
        if progressView == nil {
            progressView = GradientCircularProgress()
            progressView?.show(message: text)
        } else {
            progressView?.updateMessage(message: text)
        }
    }
    
    fileprivate func hideProgress() {
        progressView?.dismiss()
        progressView = nil
    }
    
    fileprivate func alert(error: Error?, ellipse: Ellipse?) {
        hideProgress()
        let al = UIAlertController(title: "Warning", message: error?.localizedDescription, preferredStyle: .alert)
        al.addAction(.init(title: "OK", style: .cancel, handler: nil))
        if let e = error as? EllipseError, case .accessDenided = e {
            al.addAction(.init(title: "Unlock with pin", style: .default, handler: { (_) in
                self.lockedEllipse = ellipse
                let controller = self.storyboard?.instantiateViewController(withIdentifier: "pinvc") as! PinViewController
                controller.delegate = self
                self.present(controller, animated: true, completion: nil)
            }))
        }
        present(al, animated: true, completion: nil)
    }
}

extension RootViewController: PinViewControllerDelegate {
    func save(pin: [Pin], completion: @escaping (Error?) -> ()) {
        lockedEllipse?.unlock(with: pin.map(Ellipse.Pin.init))
        completion(nil)
    }
}

extension RootViewController: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didRestoreConnected locks: [Ellipse]) {
        locks.forEach {$0.subscribe(self)}
    }
    
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Ellipse], delete: [Ellipse]) {
        self.locks = lockManager.locks
        tableView.reloadData()
    }
    
    func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool) {
        
    }
}

extension RootViewController: EllipseDelegate {    
    func ellipse(_ ellipse: Ellipse, didUpdate connection: Ellipse.Connection) {
        guard let idx = locks.firstIndex(of: ellipse) else { return }
        let indexPath = IndexPath(row: idx, section: 0)
        switch connection {
        case .paired:
            connected.insert(ellipse)
            tableView.reloadRows(at: [indexPath], with: .automatic)
            hideProgress()
//            ellipse.isMagnetAutoLockEnabled = true
        case .connecting:
            progress(text: "Connecting...")
        case .unpaired where connected.contains(ellipse):
            ellipse.unsubscribe(self)
            connected.remove(ellipse)
            tableView.reloadRows(at: [indexPath], with: .automatic)
            hideProgress()
        case .failed(let error):
            alert(error: error, ellipse: ellipse)
        default:
            break
        }
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate security: Ellipse.Security) {
        
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate value: Ellipse.Value) {
    }
    
    func ellipseDidReset(_ ellipse: Ellipse) {
        switch ellipse.connection {
        case .paired:
            ellipse.disconnect()
        default:
            break
        }
    }
}

extension UIViewController {
    func warn(error: Error?) {
        let al = UIAlertController(title: "Warning", message: error?.localizedDescription, preferredStyle: .alert)
        al.addAction(.init(title: "OK", style: .cancel, handler: nil))
        present(al, animated: true, completion: nil)
    }
}
