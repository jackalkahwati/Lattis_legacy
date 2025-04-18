//
//  AxaLockDispatchViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 19.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import UIKit
import AXALock
import Cartography
import Oval
import CoreLocation

class AxaLockDispatchViewController: ViewController {
    
    let device: AxaDevice
    fileprivate let handler = AxaBLE.Handler()
    fileprivate var currentState: BikeState = .outOfService
    fileprivate let container = UIStackView()
    fileprivate let network: BikeNetwork = Session.shared
    fileprivate let locationManager = CLLocationManager()
    fileprivate var currentLocation: CLLocation?
    fileprivate let lockSwitch = AxaLockControl(unlockedColor: .lsTurquoiseBlue, lockedImage: UIImage(named: "icon_lock_secure"), unlockedImage: UIImage(named: "icon_lock_unsecure"))
    fileprivate var categories: [ArchiveCategory] = [
        .defleet,
        .total_loss,
        .stollen,
    ]
    
    init(_ device: AxaDevice) {
        self.device = device
        super.init(nibName: nil, bundle: nil)
        handler.connectionChanged = { [unowned self] lock in
            guard lock.connection == .disconnected else { return }
            self.navigationController?.popToRootViewController(animated: true)
        }
        handler.statusChanged = { [unowned self] lock in
            self.handle(status: lock.status)
        }
        handler.add(device.lock)
        locationManager.delegate = self
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "locks_action_dispatch".localized()
        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
        navigationItem.rightBarButtonItem = .init(title: "locks_action_disconnect".localized(), style: .plain, target: self, action: #selector(disconnect))
        view.backgroundColor = .white
        
        view.addSubview(lockSwitch)
        handle(status: device.lock.status)
        lockSwitch.addTarget(self, action: #selector(valueChanged(control:)), for: .valueChanged)
        
        container.axis = .vertical
        container.spacing = .margin/2

        view.addSubview(container)
        constrain(container, lockSwitch, view) { con, lock, view in
            con.bottom == view.safeAreaLayoutGuide.bottom - .margin
            con.left == view.left + .margin
            con.right == view.right - .margin
            
            lock.centerX == view.centerX
            lock.bottom == con.top - .margin
        }
        
        if device.bike != nil {
            let nameLabel = UILabel()
            nameLabel.textAlignment = .center
            nameLabel.text = device.bike?.name
            container.addArrangedSubview(nameLabel)
            container.addArrangedSubview(button(for: .live, action: #selector(sendToLive(_:))))
            container.addArrangedSubview(button(for: .staging, action: #selector(sendToStaging(_:))))
            container.addArrangedSubview(button(for: .outOfService, action: #selector(sendToOutOfService(_:))))
            container.addArrangedSubview(button(for: .archived(.defleet), action: #selector(sendToArchive(_:))))
        }
        locationManager.startUpdatingLocation()
    }
    
    @objc
    fileprivate func back() {
        navigationController?.popViewController(animated: true)
    }
    
    @objc
    fileprivate func disconnect() {
        device.lock.disconnect()
    }
    
    @objc
    fileprivate func valueChanged(control: AxaLockControl) {
        if control.isLocked {
            device.lock.lock()
        } else {
            device.lock.unlock()
        }
    }
    
    fileprivate func handle(status: AxaBLE.Lock.Status) {
        switch status {
        case .open:
            lockSwitch.isProcessing = false
            lockSwitch.set(isLocked: false)
        case .strongClosed:
            lockSwitch.isProcessing = false
            lockSwitch.set(isLocked: true)
        case .unsecuredOpen, .weakClosed:
            lockSwitch.isProcessing = true
        default:
            break
        }
    }
    
    fileprivate func button(for state: BikeState, action: Selector) -> UIButton {
        let button = UIButton(type: .custom)
        button.setTitle(state.display, for: .normal)
        button.addTarget(self, action: action, for: .touchUpInside)
        button.layer.cornerRadius = 5
        button.layer.borderWidth = 2
        button.tag = state.tag
        let isCurrent = state.tag == currentState.tag
        let color: UIColor = isCurrent ? .white : .lightGray
        button.backgroundColor = isCurrent ? .lsTurquoiseBlue : .clear
        button.layer.borderColor = color.cgColor
        button.setTitleColor(color, for: .normal)
        constrain(button) { $0.height == 44 }
        return button
    }
    
    fileprivate func didUpdate(state: BikeState) {
        stopLoading { [unowned self] in
            self.disconnect()
        }
        currentState = state
        for view in container.arrangedSubviews {
            guard let button = view as? UIButton else { continue }
            let isCurrent = view.tag == currentState.tag
            let color: UIColor = isCurrent ? .white : .lightGray
            button.backgroundColor = isCurrent ? .lsTurquoiseBlue : .clear
            view.layer.borderColor = color.cgColor
            button.setTitleColor(color, for: .normal)
        }
    }
    
    @objc
    fileprivate func sendToLive(_ sender: UIButton) {
        guard sender.tag != currentState.tag else { return }
        guard device.lock.status == .strongClosed else {
            let alert = AlertController(title: "dispatch_lock_state_title".localized(), message: "dispatch_lock_state_subtitle".localized())
            alert.actions = [.ok]
            present(alert, animated: true, completion: nil)
            return
        }
        update(state: .live)
    }
    
    @objc
    fileprivate func sendToStaging(_ sender: UIButton) {
        guard sender.tag != currentState.tag else { return }
        update(state: .staging)
    }
    
    @objc
    fileprivate func sendToOutOfService(_ sender: UIButton) {
        guard sender.tag != currentState.tag else { return }
        update(state: .outOfService)
    }
    
    @objc
    fileprivate func sendToArchive(_ sender: UIButton) {
        guard sender.tag != currentState.tag else { return }
        let dialog = ArchiveDialog.create(title: "dispatch_status_dialog_title".localized(), subtitle: "dispatch_archive_dialog_text".localized())
        dialog.picker.delegate = self
        dialog.confirm = { [unowned self] in
            let cat = self.categories[dialog.picker.selectedRow(inComponent: 0)]
            self.update(state: .archived(cat))
        }
        dialog.show()
    }
    
    fileprivate func update(state: BikeState) {
        guard let location = currentLocation,
            let bikeId = device.bike?.bikeId else { return }
        let action: () -> () = { [unowned self] in
            self.startLoading()
            self.network.updae(state: state, with: location.coordinate, for: bikeId) { [weak self] (result) in
                switch result {
                case .success:
                    self?.didUpdate(state: state)
                case .failure(let err):
                    self?.show(error: err)
                }
            }
        }
        var subtitle = "dispatch_status_dialog_text".localizedFormat(state.display)
        if case .live = state {
            subtitle = subtitle + "\n" + "dispatch_status_dialog_text_live".localized()
        }
        let controller = AlertController(title: "dispatch_status_dialog_title".localized(), message: subtitle)
        controller.actions = [
            .plain(title: "confirm".localized(), handler: action),
            .cancel
        ]
        present(controller, animated: true, completion: nil)
    }
}

extension AxaLockDispatchViewController: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        currentLocation = locations.first
    }
}

extension AxaLockDispatchViewController: UIPickerViewDelegate, UIPickerViewDataSource {
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return categories.count
    }
    
    func pickerView(_ pickerView: UIPickerView, attributedTitleForRow row: Int, forComponent component: Int) -> NSAttributedString? {
        return NSAttributedString(string: categories[row].display, attributes: [NSAttributedString.Key.foregroundColor: UIColor.white, NSAttributedString.Key.font: UIFont.systemFont(ofSize: 14)])
    }
}

fileprivate extension BikeState {
    var tag: Int {
        switch self {
        case .live:
            return 0
        case .staging:
            return 1
        case .outOfService:
            return 2
        case .archived:
            return 3
        }
    }
}
