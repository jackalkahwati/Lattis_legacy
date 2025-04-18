//
//  AxaLockAssignViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 27.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import UIKit
import Oval
import Cartography

class AxaLockAssignViewController: ViewController {

    fileprivate let titleLabel = UILabel()
    fileprivate let groupButton = UIButton(type: .system)
    fileprivate let qrCodeView: QRCodeView<QRCodeBike> = .json()
    fileprivate let assignButton = ActionButton()
    fileprivate let scanButton = ActionButton()
    fileprivate let device: AxaDevice
    fileprivate let completion: (Bike) -> ()
    fileprivate let network: BikeNetwork = Session.shared
    fileprivate var qrCode: QRCodeBike?
    fileprivate var group: Group? {
        didSet {
            groupButton.setTitle(group?.display, for: .normal)
        }
    }
    fileprivate let storage = CoreDataStack.shared
    fileprivate var groups = [Group]()
    fileprivate let assignActions = ActionButton.ActionTuple(active: .plain(title: "assign".localized(), style: .active), inactive: .plain(title: "assign".localized()))
    fileprivate let scanActions = ActionButton.ActionTuple(active: .plain(title: "scan".localized(), style: .active), inactive: .plain(title: "scan".localized()))
    fileprivate let bikeContainer = UIStackView()
    
    init(_ device: AxaDevice, completion: @escaping (Bike) -> ()) {
        self.device = device
        self.completion = completion
        super.init(nibName: nil, bundle: nil)
        qrCodeView.completion = { [unowned self] status in
            self.handleQR(status: status)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close))
        view.addSubview(qrCodeView)
        qrCodeView.layer.cornerRadius = 10
        
        assignButton.action = assignActions.inactive
        scanButton.action = scanActions.inactive
        assignButton.layer.cornerRadius = 5
        scanButton.layer.cornerRadius = 5
        
        let container = UIStackView(arrangedSubviews: [assignButton, scanButton])
        container.axis = .horizontal
        container.distribution = .fillEqually
        container.spacing = .margin/2
        view.addSubview(container)
        
        bikeContainer.axis = .vertical
        bikeContainer.spacing = .margin
        bikeContainer.distribution = .fill
        let nameLabel = UILabel()
        nameLabel.text = "settings_bike_name".localized()
        nameLabel.textColor = .gray
        nameLabel.font = .systemFont(ofSize: 14)
        titleLabel.font = .boldSystemFont(ofSize: 14)
        let bike = UIStackView(arrangedSubviews: [nameLabel, titleLabel])
        titleLabel.textAlignment = .right
        bike.spacing = bikeContainer.spacing
        bikeContainer.addArrangedSubview(bike)
        if let bike = device.bike {
            let oldBikeLabel = UILabel()
            oldBikeLabel.font = .systemFont(ofSize: 14)
            oldBikeLabel.text = "old_bike_name".localized()
            oldBikeLabel.textColor = .gray
            let bikeLabel = UILabel()
            bikeLabel.text = bike.name
            bikeLabel.font = .boldSystemFont(ofSize: 14)
            let ctn = UIStackView(arrangedSubviews: [oldBikeLabel, bikeLabel])
            bikeContainer.addArrangedSubview(ctn)
        } else {
            let groupLabel = UILabel()
            groupLabel.text = "vehicle_type".localized()
            groupLabel.textColor = .gray
            groupLabel.font = .systemFont(ofSize: 14)
            groupButton.titleLabel?.font = .boldSystemFont(ofSize: 14)
            let groupContainer = UIStackView(arrangedSubviews: [groupLabel, groupButton])
            bikeContainer.addArrangedSubview(groupContainer)
        }
        view.addSubview(bikeContainer)
        bikeContainer.isHidden = true
        
        constrain(qrCodeView, container, bikeContainer, view) { code, container, bike, view in
            code.left == view.left + .margin
            code.right == view.right - .margin
            code.height == code.width
            code.centerX == view.centerX
            code.bottom == container.top - .margin
            
            container.left == code.left
            container.right == code.right
            container.bottom == view.safeAreaLayoutGuide.bottom - .margin
            container.height == 44
            
            bike.top == view.safeAreaLayoutGuide.top + .margin
            bike.left == code.left
            bike.right == code.right
        }
        
        guard let fleet = storage.currentFleet else { return }
        network.getGroups(for: fleet) { [weak self] (result) in
            switch result {
            case .success(let gr):
                if gr.isEmpty {
                    
                }
                self?.group = gr.first
                self?.groups = gr
            case .failure(let error):
                self?.show(error: error)
            }
        }
        qrCodeView.startScan()
        
        scanButton.addTarget(self, action: #selector(scan), for: .touchUpInside)
        assignButton.addTarget(self, action: #selector(assign), for: .touchUpInside)
        groupButton.addTarget(self, action: #selector(selectGroup), for: .touchUpInside)
    }
    
    @objc
    fileprivate func close() {
        dismiss(animated: true, completion: nil)
    }
    
    @objc
    fileprivate func scan() {
        qrCodeView.startScan()
        qrCode = nil
        bikeContainer.isHidden = true
        assignButton.action = assignActions.inactive
        scanButton.action = scanActions.inactive
    }
    
    @objc
    fileprivate func selectGroup() {
        guard let qr = qrCode else { return }
        let dialog = ArchiveDialog.create(title: String(format: "settings_group_select_title".localized(), qr.name), subtitle: "settings_group_select_subtitle".localized())
        dialog.confirmTitle = "settings_group_select_confirm".localized()
        dialog.confirm = { [unowned self] in
            self.group = self.groups[dialog.picker.selectedRow(inComponent: 0)]
        }
        dialog.picker.delegate = self
        dialog.picker.dataSource = self
        if let gr = group, let idx = groups.firstIndex(where: {$0.groupId == gr.groupId}) {
            dialog.picker.selectRow(idx, inComponent: 0, animated: false)
        }
        dialog.show()
    }
    
    @objc
    fileprivate func assign() {
        guard let qr = qrCode else { return }
        startLoading()
        if let bike = device.bike {
            network.changeIoTLabel(for: bike.bikeId, qrCode: qr) { [weak self] (result) in
                switch result {
                case .failure(let error):
                    self?.show(error: error)
                case .success:
                    self?.stopLoading()
                    self?.completion(bike.update(qr: qr))
                }
            }
        } else if let group = group {
            network.assign(bikeWith: qr, and: group, to: device.module) { [weak self] (result) in
                switch result {
                case .success(let bike):
                    self?.stopLoading()
                    self?.completion(bike)
                case .failure(let error):
                    self?.show(error: error)
                }
            }
        }
    }
    
    fileprivate func handleQR(status: QRCodeView<QRCodeBike>.State) {
        guard case let .code(qr) = status else { return }
        qrCode = qr
        qrCodeView.stopScan()
        titleLabel.text = qr.name
        bikeContainer.isHidden = false
        assignButton.action = assignActions.active
        scanButton.action = scanActions.active
    }
}

extension AxaLockAssignViewController: UIPickerViewDelegate, UIPickerViewDataSource {
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return groups.count
    }
    
    func pickerView(_ pickerView: UIPickerView, attributedTitleForRow row: Int, forComponent component: Int) -> NSAttributedString? {
        return NSAttributedString(string: groups[row].display, attributes: [NSAttributedString.Key.foregroundColor: UIColor.white, NSAttributedString.Key.font: UIFont.systemFont(ofSize: 14)])
    }
}

extension Bike {
    func update(qr: QRCodeBike) -> Bike {
        .init(bikeId: bikeId, qrCodeId: Int(qr.id), fleetId: fleetId, lockId: lockId, name: qr.name, status: status)
    }
}
