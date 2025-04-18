//
//  DispatchDispatchViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

public enum ArchiveCategory: String, Encodable {
    case defleet, total_loss, stollen
    var display: String {
        switch self {
        case .defleet: return "bike_archive_category_defleet".localized()
        case .stollen: return "bike_archive_category_stolen".localized()
        case .total_loss: return "bike_archive_category_total_loss".localized()
        }
    }
}

public enum BikeState {
    case live, staging, outOfService, archived(ArchiveCategory)
    
    var key: String {
        switch self {
        case .live: return "live"
        case .outOfService: return "out_of_service"
        case .staging: return "staging"
        case .archived(_): return "archive"
        }
    }
    
    var display: String {
        switch self {
        case .live: return "bike_state_live".localized()
        case .outOfService: return "bike_state_out_of_service".localized()
        case .staging: return "bike_state_staging".localized()
        case .archived(_): return "bike_state_archive".localized()
        }
    }
}

class DispatchViewController: ViewController {
    
    @IBOutlet weak var actionsHeightLayout: NSLayoutConstraint!
    @IBOutlet weak var slider: LockSlider!
    @IBOutlet weak var tableView: UITableView!
    
    var interactor: DispatchInteractorInput!
    
    fileprivate var states: [BikeState] = [
        .live,
        .staging,
        .outOfService,
        .archived(.defleet)
    ]
    fileprivate var categories: [ArchiveCategory] = [
        .defleet,
        .total_loss,
        .stollen,
    ]

    override func viewDidLoad() {
        super.viewDidLoad()

        navigationItem.leftBarButtonItem = UIBarButtonItem(title: nil, style: .plain, target: nil, action: nil)
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "locks_action_disconnect".localized(), style: .plain, target: self, action: #selector(disconnect))
        
        tableView.dataSource = self
        tableView.delegate = self
        
        interactor.viewLoaded()
    }
    
    @objc private func back() {
        _ = navigationController?.popViewController(animated: true)
    }
    
    @objc func disconnect() {
        let dialog = DialogView.create(title: "dispatch_disconnect_dialog_title".localized(), subtitle: "dispatch_disconnect_dialog_text".localized())
        dialog.confirm = { [unowned self] in
            self.interactor.finish()
        }
        dialog.show()
    }
    
    @IBAction func lockStateChanged(_ sender: LockSlider) {
        if case .processing(let state) = sender.lockState {
            interactor.set(lockState: state)
        }
    }
}

extension DispatchViewController: DispatchInteractorOutput {
    func show(lock: Lock) {
        title = lock.name
        tableView.isHidden = lock.lock?.bikeId == nil
        
        let attributes: [String: Any] = ["lock": lock.lock?.lockId ?? "nil", "bike": lock.lock?.bikeId ?? "nil"]
    }
    
    func didSelect(state: BikeState, with error: Error?) {
        tableView.reloadData()
    }
    
    func update(state: LockSlider.LockState) {
        slider.lockState = state
    }
    
    func showBack() {
        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
    }
}

extension DispatchViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return states.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "action", for: indexPath) as! DispatchActionCell
        let state = states[indexPath.row]
        cell.state = state
        cell.selectImageView.isHighlighted = interactor.isCurrent(state: state)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let action = states[indexPath.row]
        switch action {
        case .archived:
            let dialog = ArchiveDialog.create(title: "dispatch_status_dialog_title".localized(), subtitle: "dispatch_archive_dialog_text".localized())
            dialog.picker.delegate = self
            dialog.confirm = {
                let cat = self.categories[dialog.picker.selectedRow(inComponent: 0)]
                self.interactor.select(state: .archived(cat))
            }
            dialog.show()
        default:
            var subtitle = "dispatch_status_dialog_text".localizedFormat(action.display)
            if case .live = action {
                subtitle = subtitle + "\n" + "dispatch_status_dialog_text_live".localized()
            }
            let dialog = DialogView.create(title: "dispatch_status_dialog_title".localized(), subtitle: subtitle)
            dialog.confirm = {
                self.interactor.select(state: action)
            }
            if case .live = action, interactor.isLockLocked ==  false {
                let lockAlert = ErrorAlertView.alert(title: "dispatch_lock_state_title".localized(), subtitle: "dispatch_lock_state_subtitle".localized())
                lockAlert.show()
            } else {
                dialog.show()
            }
        }
    }
}

extension DispatchViewController: UIPickerViewDelegate, UIPickerViewDataSource {
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
