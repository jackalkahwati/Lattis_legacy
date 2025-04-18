//
//  ProfileProfileViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/04/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import PhoneNumberKit

class ProfileViewController: ViewController {
    @IBOutlet weak var blurView: UIVisualEffectView!
    @IBOutlet weak var smallImageView: UIImageView!
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var smallPhotoButton: UIButton!
    @IBOutlet weak var photoButton: UIButton!
    @IBOutlet weak var tableView: UITableView!
    var interactor: ProfileInteractorInput!
    var hasFleets: Bool = false

    fileprivate var sections: [BikeInfoViewController.Section] = []
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        navigationItem.leftBarButtonItem = .menu(target: self, action: #selector(menu))
        title = "profile_title".localized()
        navigationController?.navigationBar.set(style: .blue)
        navigationController?.setNavigationBarHidden(false, animated: true)
        
        tableView.dataSource = self
        tableView.delegate = self
        tableView.register(BikeInfoSectionHeader.self, forHeaderFooterViewReuseIdentifier: BikeInfoSectionHeader.identifier)
        tableView.register(BikeInfoSectionFooter.self, forHeaderFooterViewReuseIdentifier: BikeInfoSectionFooter.identifier)
        tableView.tableFooterView = UIView()
        
        blurView.effect = nil
        interactor.viewLoaded()
    }
        
    @IBAction func photo(_ sender: Any) {
        func picker(type: UIImagePickerController.SourceType) {
            let picker = UIImagePickerController()
            picker.sourceType = type
            picker.delegate = self
            present(picker, animated: true, completion: nil)
        }
        
        let action = UIAlertController(title: "general_image_picker_source_title".localized(), message: nil, preferredStyle: .actionSheet)
        action.addAction(UIAlertAction(title: "general_image_picker_source_camera".localized(), style: .default, handler: { _ in
            picker(type: .camera)
        }))
        action.addAction(UIAlertAction(title: "general_image_picker_source_library".localized(), style: .default, handler: { _ in
            picker(type: .photoLibrary)
        }))
        action.addAction(UIAlertAction(title: "general_btn_cancel".localized(), style: .cancel, handler: nil))
        present(action, animated: true, completion: nil)
    }
    
    var profileImage: UIImage? {
        get {
            return smallImageView.image
        }
        set {
            smallImageView.image = newValue
            imageView.image = newValue
            
            photoButton.isHidden = newValue != nil
            smallPhotoButton.isHidden = newValue == nil
            smallImageView.isHidden = newValue == nil
            blurView.effect = newValue == nil ? nil : UIBlurEffect(style: .light)
        }
    }
}

extension ProfileViewController: ProfileInteractorOutput {
    func show(_ user: User) {
        profileImage = user.image
        var networks = [ProfileCell.RowModel.empty("profile_private_networks_empty".localized())]
        if user.privateNetworks.isEmpty == false {
            networks = user.privateNetworks.map{ ProfileCell.RowModel.network($0) }
        }
        hasFleets = user.privateNetworks.isEmpty == false
        networks.append(ProfileCell.RowModel.info(ProfileInfoModel(type: .privateNetworks, keyboard: .emailAddress, value: "profile_add_private_network".localized(), action: interactor.update(value: for: ))))
        sections = [
            BikeInfoViewController.Section(title: "profile_personal_info".localized(), items: [
                ProfileCell.RowModel.info(ProfileInfoModel(type: .name, value: user.firstName, action: interactor.update(value: for: ))),
                ProfileCell.RowModel.info(ProfileInfoModel(type: .lastName, value: user.lastName, action: interactor.update(value: for: ))),
                ProfileCell.RowModel.info(ProfileInfoModel(type: .email, keyboard: .emailAddress, value: user.email, accessoryType: .disclosureIndicator, action: interactor.update(value: for: ))),
                ProfileCell.RowModel.info(ProfileInfoModel(type: .phone, keyboard: .phonePad, value: user.formattedNumber, accessoryType: .disclosureIndicator, action: interactor.update(value: for: )))
                ]),
            BikeInfoViewController.Section(title: "profile_security".localized(), items: [
                ProfileCell.RowModel.info(ProfileInfoModel(type: .password, value: "profile_chnge_password".localized(), action: interactor.update(value: for: ))),
//                ProfileCell.RowModel.info(ProfileInfoModel(type: .delete, value: "profile_delete_account".localized(), action: interactor.update(value: for: )))
                ]),
            BikeInfoViewController.Section(title: "profile_private_networks".localized(), items: networks)
        ]
        
        tableView.reloadData()
    }
}

extension ProfileViewController: UITableViewDataSource, UITableViewDelegate {
    func numberOfSections(in tableView: UITableView) -> Int {
        return sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return sections[section].items.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let model = sections[indexPath.section].items[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: model.identifire, for: indexPath) as! ProfileCell
        cell.model = (model as? ProfileCell.RowModel)
        return cell
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 58
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let header = tableView.dequeueReusableHeaderFooterView(withIdentifier: BikeInfoSectionHeader.identifier) as! BikeInfoSectionHeader
        header.titleLabel.text = sections[section].title
        return header
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 44
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        return UIView()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let viewModel = sections[indexPath.section].items[indexPath.row] as! ProfileCell.RowModel
        guard case var .info(model) = viewModel else { return }
        switch model.type {
        case .name, .lastName:
            break
        case .password:
            interactor.changePassword()
        case .delete:
            guard AppRouter.shared.isTripStarted == false else {
                let alert = ActionAlertView.alert(title: "delete_account_ride_warning_title".localized(), subtitle: "delete_account_ride_warning_text".localized())
                alert.action = AlertAction(title: "logout_ride_warning_action".localized(), action: {
                    AppRouter.shared.home()
                    AppRouter.shared.endTrip(false)
                })
                alert.cancel = AlertAction(title: "logout_ride_warning_cancel".localized(), action: {})
                alert.show()
                return
            }
            let alert = ActionAlertView.alert(title: "delete_account_title".localized(), subtitle: "delete_account_text".localized())
            alert.action = AlertAction(title: "delete_account_submit".localized(), action: interactor.deleteAccount)
            alert.cancel = AlertAction(title: "delete_account_cancel".localized(), action: {})
            alert.show()
        case .privateNetworks:
            model.value = nil
            fallthrough
        default:
            interactor.edit(info: model)
        }
    }
}

extension ProfileViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true, completion: nil)
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        guard let image = info[.originalImage] as? UIImage else { return }
        profileImage = image
        interactor.save(image: image)
        picker.dismiss(animated: true, completion: nil)
    }
}

extension User {
    var formattedNumber: String? {
        guard let phone = phoneNumber else { return phoneNumber }
        let region = Locale.current.regionCode ?? "US"
        guard let parsed = try? PhoneNumberKit().parse(phone, withRegion: region, ignoreType: true) else { return phoneNumber }
        let number = "+\(String(parsed.countryCode))\(String(parsed.nationalNumber))"
        return PartialFormatter().formatPartial(number)
    }
}
