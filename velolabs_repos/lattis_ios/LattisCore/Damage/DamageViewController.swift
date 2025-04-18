//
//  DamageViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 18/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class DamageViewController: UIViewController {
    
    fileprivate let bikeId: Int
    fileprivate let tripId: Int?
    
    init(_ bikeId: Int, tripId: Int? = nil) {
        self.bikeId = bikeId
        self.tripId = tripId
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate let scrollView = UIScrollView()
    fileprivate let stackView = UIStackView()
    fileprivate let noteField = UITextField()
    fileprivate let photoView = UIImageView()
    fileprivate let submitButton = ActionButton()
    fileprivate let network: ServiceNetwork = AppRouter.shared.api()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }

        title = "report_damage".localized()
        addCloseButton()
        view.backgroundColor = .white
        view.addSubview(scrollView)
        scrollView.addSubview(stackView)
        
        stackView.axis = .vertical
        stackView.spacing = .margin/2
        stackView.isLayoutMarginsRelativeArrangement = true
        stackView.directionalLayoutMargins = .init(top: .margin*2, leading: .margin, bottom: .margin*2 + 44, trailing: .margin)
        stackView.layoutMargins = .init(top: .margin*2, left: .margin, bottom: .margin*2 + 44, right: .margin)
        
        view.addSubview(submitButton)
        
        constrain(scrollView, stackView, submitButton, view) { scroll, stack, submit, view in
            scroll.top == view.safeAreaLayoutGuide.top
            scroll.left == view.left
            scroll.right == view.right
            scroll.bottom == submit.top - .margin/2
            
            submit.bottom == view.safeAreaLayoutGuide.bottom - .margin
            submit.left == view.left + .margin
            submit.right == view.right - .margin
            
            stack.edges == scroll.edges
            stack.width == view.width
        }
        
        noteField.attributedPlaceholder = NSAttributedString(string: "tap_to_enter_notes".localized(), attributes: [.font: UIFont.theme(weight: .book, size: .body)])
        noteField.font = .theme(weight: .book, size: .body)
        noteField.returnKeyType = .done
        noteField.delegate = self
        
        let noteLabel = UILabel.label(text: "damage_report_notes".localized(), font: .theme(weight: .medium, size: .body))
        let photoLabel = UILabel.label(text: "damage_report_photo".localized(), font: .theme(weight: .medium, size: .body))
        let emptyLabel = UILabel.label(text: "no_photo".localized(), font: .theme(weight: .book, size: .body), color: .lightGray, allignment: .center)
        constrain(emptyLabel) { $0.height == 200 }

        stackView.addArrangedSubview(noteLabel)
        stackView.addArrangedSubview(noteField)
        stackView.addArrangedSubview(.line)
        stackView.addArrangedSubview(photoLabel)
        stackView.addArrangedSubview(emptyLabel)
        stackView.setCustomSpacing(.margin, after: photoLabel)
        
        photoView.layer.cornerRadius = .containerCornerRadius
        photoView.clipsToBounds = true
        photoView.contentMode = .scaleAspectFill
        constrain(photoView) { $0.height == 350 }
        
        submitButton.action = .plain(title: nil, icon: .named("icon_end_ride_photo")) { [unowned self] in self.takePhoto() }
    }
    
    fileprivate func takePhoto() {
        let source: UIImagePickerController.SourceType = .camera
        guard UIImagePickerController.isSourceTypeAvailable(source) else {
            return
        }
        let picker = UIImagePickerController()
        picker.delegate = self
        picker.sourceType = source
        present(picker, animated: true, completion: nil)
    }
    
    fileprivate func submit(damage: Damage) {
        startLoading("damage_report_loader".localized())
        network.report(damage: damage) { [weak self] (result) in
            switch result {
            case .success:
                let alert = AlertController(title: "damage_report_success_title".localized(), message: .plain("damage_report_success_message".localized()))
                if self?.tripId == nil { // Bike reserved (Trip not started)
                    alert.actions.append(.plain(title: "damage_report_success_continue_booking".localized()) { [unowned self] in
                        self?.dismiss(animated: true, completion: nil)
                    })
                    alert.actions.append(.plain(title: "damage_report_success_cancel_booking".localized(), style: .inactive) { [unowned self] in
                        self?.dismiss(animated: true, completion: {
                            NotificationCenter.default.post(name: .cancelBooking, object: nil)
                        })
                    })
                } else { // Trip started
                    alert.actions.append(.plain(title: "damage_report_success_continue_ride".localized()) { [unowned self] in
                        self?.dismiss(animated: true, completion: nil)
                    })
                    alert.actions.append(.plain(title: "end_ride".localized(), style: .inactive) { [unowned self] in
                        self?.dismiss(animated: true, completion: {
                            NotificationCenter.default.post(name: .endRide, object: nil, userInfo: [Notification.UserInfoKey.damageReported: true])
                        })
                    })
                }
                self?.stopLoading {
                    self?.present(alert, animated: true, completion: nil)
                }
            case .failure(let error):
                self?.handle(error)
            }
            self?.stopLoading()
        }
    }
    
    fileprivate func update(photo: UIImage) {
        if photoView.image == nil, let empty = stackView.arrangedSubviews.last {
            stackView.removeArrangedSubview(empty)
            empty.removeFromSuperview()
            stackView.addArrangedSubview(photoView)
        }
        photoView.image = photo
        submitButton.action = .plain(title: "damage_report_title".localized()) { [unowned self] in
            self.submit(damage: .init(category: .damageCategoryOther, notes: self.noteField.text ?? "", image: photo.resize(to: .init(width: 800, height: 800)).jpegData(compressionQuality: 2)!, bikeId: bikeId, tripId: tripId))
        }
    }
}

extension DamageViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate, UITextFieldDelegate {
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        dismiss(animated: true, completion: nil)
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        dismiss(animated: true, completion: nil)
        guard let image = info[.originalImage] as? UIImage else { return }
        update(photo: image)
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
}
