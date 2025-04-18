//
//  AxaLockQRScannerViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 20.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import UIKit
import Cartography
import AXALock
import Oval

class AxaLockQRScannerViewController: ViewController {
    
    fileprivate let finished: (AxaDevice) -> ()
    fileprivate let titleLabel = UILabel()
    fileprivate let onboardButton = ActionButton()
    fileprivate let scanButton = ActionButton()
    fileprivate let qrReaderView: QRCodeView<String> = .string
    fileprivate var claim: AxaCloud.Claim?
    fileprivate let handler = AxaBLE.Handler()
    fileprivate let network: IoTNetwork = Session.shared
    fileprivate var locks = AxaBLE.Lock.all
    fileprivate let storage = CoreDataStack.shared
    fileprivate let warningLabel = UILabel()
    fileprivate let onboardActions = ActionButton.ActionTuple(active: .plain(title: "onboard".localized(), style: .active), inactive: .plain(title: "onboard".localized()))
    fileprivate let scanAcitons = ActionButton.ActionTuple(active: .plain(title: "scan".localized(), style: .active), inactive: .plain(title: "scan".localized()))

    init(_ completion: @escaping (AxaDevice) -> ()) {
        finished = completion
        super.init(nibName: nil, bundle: nil)
        qrReaderView.completion = { [unowned self] state in
            self.handleQR(state: state)
        }
        handler.discovered = { [unowned self] lock in
            self.locks.append(lock)
        }
        handler.lockInfoUpdated = { [unowned self] lock in
            if lock.isMetadataComplete {
                self.onboard(lock)
            }
        }
        AxaBLE.Lock.scan(with: handler)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        onboardButton.action = onboardActions.inactive
        scanButton.action = scanAcitons.inactive
        title = "scan_qr_code".localized()
        view.backgroundColor = .white
        view.addSubview(titleLabel)
        view.addSubview(warningLabel)
        view.addSubview(qrReaderView)
        
        onboardButton.layer.cornerRadius = 5
        titleLabel.textAlignment = .center
        qrReaderView.layer.cornerRadius = 5
        
        let container = UIStackView(arrangedSubviews: [onboardButton, scanButton])
        container.axis = .horizontal
        container.spacing = .margin/2
        container.distribution = .fillEqually
        view.addSubview(container)
        
        warningLabel.textAlignment = .center
        warningLabel.textColor = .red
        warningLabel.font = .systemFont(ofSize: 12)
        warningLabel.text = "lock_warning_already_used".localized()
        warningLabel.isHidden = true
        
        constrain(titleLabel, warningLabel, qrReaderView, container, view) { title, warning, scanner, action, view in
            title.top == view.safeAreaLayoutGuide.top + .margin
            title.left == view.left + .margin
            title.right == view.right - .margin
            
            warning.top == title.bottom + .margin/2
            warning.left == title.left
            warning.right == title.right
            
            scanner.center == view.center
            scanner.left == view.left + .margin
            scanner.right == view.right - .margin
            scanner.height == scanner.width
            
            action.bottom == view.safeAreaLayoutGuide.bottom - .margin
            action.left == view.left + .margin
            action.right == view.right - .margin
            action.height == 44
        }
        
        scanButton.addTarget(self, action: #selector(scan), for: .touchUpInside)
        onboardButton.addTarget(self, action: #selector(handleAction), for: .touchUpInside)
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close))
        qrReaderView.startScan()
    }
    
    @objc
    fileprivate func close() {
        dismiss(animated: true, completion: nil)
    }
    
    @objc
    fileprivate func scan() {
        guard !qrReaderView.isScanning else { return }
        scanButton.action = scanAcitons.inactive
        qrReaderView.startScan()
        titleLabel.text = nil
        warningLabel.isHidden = true
    }
    
    @objc
    fileprivate func handleAction() {
        guard let c = claim, let lock = locks.first(where: \.id, isEqual: c.lock_uid) else {
            report(error: AxaError.lockIsNotAround)
            showAlert(title: claim?.lock_uid, subtitle: "AXA eRL Lock is not found. Please make sure it's around and has battery inside")
            return
        }
        startLoading()
        AxaBLE.Lock.claim(code: c) { [weak self] (result) in
            switch result {
            case .success:
                lock.connect(with: self?.handler)
            case .failure(let error):
                self?.show(error: error)
            }
        }
    }
    
    fileprivate func handleQR(state: QRCodeView<String>.State) {
        guard case let .code(qr) = state else { return }
        qrReaderView.stopScan()
        scanButton.action = scanAcitons.active
        var array = qr.components(separatedBy: "-")
        guard array.count >= 2 else { return }
        let claimCode = array.removeLast()
        let uid = array.removeLast()
        titleLabel.text = uid
        network.fetch(query: .key(uid)) { [weak self] (result) in
            switch result {
            case .success(let modules):
                if let m = modules.first, m.key == uid {
                    self?.claim = nil
                    if let action = self?.onboardActions.inactive {
                        self?.onboardButton.action = action
                    }
                    self?.warningLabel.isHidden = false
                } else {
                    if let action = self?.onboardActions.active {
                        self?.onboardButton.action = action
                    }
                    self?.claim = AxaCloud.Claim(lock_uid: uid, claim_code: claimCode)
                }
            case .failure(let error):
                self?.show(error: error)
            }
        }
    }
    
    fileprivate func onboard(_ lock: AxaBLE.Lock) {
        guard let fleet = storage.currentFleet else { return }
        network.onboard(module: .init(axa: lock, fleet: fleet.fleetId), completion: { [weak self] (result) in
            switch result {
            case .success(let module):
                self?.stopLoading()
                self?.finished(.init(lock: lock, module: module, bike: nil))
            case .failure(let error):
                self?.show(error: error)
            }
        })
    }
}

enum AxaError: Error {
    case lockIsNotAround
}

extension ActionButton {
    struct ActionTuple {
        let active: ActionButton.Action
        let inactive: ActionButton.Action
    }
}
