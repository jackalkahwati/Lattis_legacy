//
//  SettingsSettingsViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 18/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Oval

let capFixFW = "275"

class SettingsViewController: ViewController {
    @IBOutlet weak var tableView: UITableView!
    var interactor: SettingsInteractorInput!

    fileprivate var progressView: ProgressView?
    fileprivate var sections: [Section] = []
    fileprivate var groups: [Group] = []
    override func viewDidLoad() {
        super.viewDidLoad()

        tableView.sectionHeaderHeight = 56
        tableView.delegate = self
        tableView.dataSource = self
        tableView.tableFooterView = UIView()
        title = "settings_title".localized()
        
        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "locks_action_dispatch".localized(), style: .plain, target: self, action: #selector(dispatch))
        
        interactor.viewLoaded()
    }
    
    @objc private func dispatch() {
        interactor.dispatch()
    }
    
    @objc private func back() {
        _ = navigationController?.popViewController(animated: true)
    }
    
    @objc private func deleteL() {
        interactor.delete()
    }
}

extension SettingsViewController: SettingsInteractorOutput {
    func show(lock: Lock) {
        sections = lock.sections(delegate: interactor)
        tableView.reloadData()
        stopLoading()
    }
    
    func update(progress: Double) {
        if progressView == nil {
            progressView = ProgressView.show(title: "settings_firmware_update".localized())
        }
        progressView?.progress = progress
        if progress == 1 {
            progressView?.hide {
                self.startLoading(title: "Reconnecting".localized())
            }
        }
    }
    
    func showLabelDialog(completion: @escaping () -> ()) {
        let action = DialogView.create(title: "settings_change_label_title".localized(), subtitle: "settings_change_label_subtitle".localized())
        action.confirm = completion
        action.show()
    }
    
    func show(groups: [Group], bike: QRCodeBike) {
        self.groups = groups
        let dialog = ArchiveDialog.create(title: String(format: "settings_group_select_title".localized(), bike.name), subtitle: "settings_group_select_subtitle".localized())
        dialog.confirmTitle = "settings_group_select_confirm".localized()
        dialog.confirm = {
            self.interactor.select(group: self.groups[dialog.picker.selectedRow(inComponent: 0)])
        }
        dialog.picker.delegate = self
        dialog.picker.dataSource = self
        dialog.show()
    }
}

extension SettingsViewController: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        return sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return sections[section].items.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let model = sections[indexPath.section].items[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: model.reuseIdentifire, for: indexPath) as! SettingsCell
        cell.model = model
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let header = SettingsSectionView()
        header.titleLabel.text = sections[section].title
        return header
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if let cell = tableView.cellForRow(at: indexPath) as? SettingsActionCell,
            let model = cell.model as? SettingsActionCell.Model {
            model.action()
        }
    }
}

extension SettingsViewController: UIPickerViewDelegate, UIPickerViewDataSource {
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

extension SettingsViewController {
    struct Section {
        let title: String
        var items: [CellRepresentable]
    }
}

extension Lock {
    typealias Section = SettingsViewController.Section
    typealias InfoModel = SettingsInfoCell.Model
    typealias EmptyBike = SettingsEmptyBikeCell.Model
    typealias Firmware = SettingsFirmwareCell.FWModel
    typealias ActionModel = SettingsActionCell.Model
    typealias CapTouchModel = SettingsCapTouchCell.Model
    typealias AutoLockModel = SettingsAutoLockCell.Model
    typealias BatteryModel = SettingsBattryLevelCell.Model
    
    func sections(delegate: SettingsCellDelegate) -> [Section] {
        let lockInfo: [CellRepresentable] = [
            InfoModel(name:"settings_lock_name".localized(), value: name),
            InfoModel(name:"settings_lock_serial_number".localized(), callback: serialNumber),
            InfoModel(name:"settings_lock_mac_id".localized(), value: macId),
            Firmware(getFWVersion: firmware, checkFWUpdate: delegate.checkFWUpdate, delegate: delegate),
            CapTouchModel(peripheral: peripheral),
            AutoLockModel(peripheral: peripheral, action: delegate.showAutoLockAlert),
            BatteryModel(peripheral: peripheral)
        ]
        var bikeInfo: [CellRepresentable] = [EmptyBike(delegate: delegate)]
        if let bikeName = lock?.bikeName {
            bikeInfo = [
                InfoModel(name:"settings_bike_name".localized(), value: bikeName),
                ActionModel(title: "settings_unassign_bike".localized(), action: delegate.unassignBike),
                ActionModel(title: "settings_change_label".localized(), action: delegate.changeLabel)
            ]
        }
        return [
            Section(title: "settings_lock_section_title".localized(), items: lockInfo),
            Section(title: "settings_bike_section_title".localized(), items: bikeInfo)
        ]
    }
}

