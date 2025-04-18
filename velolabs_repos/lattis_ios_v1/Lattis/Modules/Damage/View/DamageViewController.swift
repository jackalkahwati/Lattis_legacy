//
//  DamageDamageViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 30/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import LGSideMenuController

class DamageViewController: ViewController {
    @IBOutlet var toolbar: UIToolbar!
    @IBOutlet weak var damageView: DamageView!
    var interactor: DamageInteractorInput!
    fileprivate var damageTypes: [DamageReport.Category] = DamageReport.Category.all
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        damageView.pickerView.dataSource = self
        damageView.pickerView.delegate = self
        
        damageView.typeField.delegate = self
        
        damageView.typeField.inputAccessoryView = toolbar
        title = "damage_report_title".localized()

        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close))
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let notes = segue.destination as? AddNotesViewController {
            notes.text = damageView.notesTextLabel.text!
            notes.saveNote = { [unowned self] text in
                self.damageView.notesTextLabel.text = text
            }
        }
    }
    
    @objc private func close() {
        dismiss(animated: true, completion: nil)
    }

    @IBAction func takePhoto(_ sender: Any) {
        let picker = UIImagePickerController()
        picker.sourceType = TARGET_OS_SIMULATOR != 0 ? .photoLibrary : .camera
        picker.delegate = self
        present(picker, animated: true, completion: nil)
    }
    
    @IBAction func typeDone(_ sender: Any) {
        view.endEditing(true)
    }
    
    @IBAction func submit(_ sender: Any) {
        interactor.submit(with: damageView.notesTextLabel.text!)
    }
}

extension DamageViewController: DamageInteractorOutput {
    func setSubmition(enabled: Bool) {
        damageView.isSubmitEnabled = enabled
    }
    
    func showSuccess() {
        func close() {
            dismiss(animated: true) {}
        }
        let alert = ActionAlertView.alert(title: "damage_report_success_title".localized(), subtitle: "damage_report_success_text".localized())
        if AppRouter.shared.isTripStarted {
            alert.action = AlertAction(title: "damage_report_success_continue_ride".localized(), action: close)
            alert.cancel = AlertAction(title: "damage_report_success_end_ride".localized(), action: interactor.endRide)
        } else {
            alert.action = AlertAction(title: "damage_report_success_continue_booking".localized(), action: close)
            alert.cancel = AlertAction(title: "damage_report_success_cancel_booking".localized(), action: interactor.cancelBooking)
        }
        alert.show()
    }
}

extension DamageViewController: UINavigationControllerDelegate, UIImagePickerControllerDelegate {
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true, completion: nil)
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        guard let image = info[.originalImage] as? UIImage else { return }
        interactor.didMake(picture: image.resizeImageWith(newSize: .side(800)))
        damageView.show(image: image)
        picker.dismiss(animated: true, completion: nil)
    }
}

extension DamageViewController: UIPickerViewDataSource, UIPickerViewDelegate {
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return damageTypes.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return damageTypes[row].displayTitle
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        let type = damageTypes[row]
        damageView.typeField.text = type.displayTitle
        interactor.didSelect(category: type)
    }
}

extension DamageViewController: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        if text == "\n" {
            textView.resignFirstResponder()
            return false
        }
        return true
    }
}

extension DamageViewController: UITextFieldDelegate {
    func textFieldDidBeginEditing(_ textField: UITextField) {
        if textField.text == nil || textField.text!.isEmpty {
            textField.text = damageTypes.first?.displayTitle
            interactor.didSelect(category: damageTypes[0])
        }
    }
}
