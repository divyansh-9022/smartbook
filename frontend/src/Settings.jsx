
import { useEffect, useState } from 'react'
import LicenseActivation from './LicenseActivation'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

function Settings({ token }) {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    businessName: '',
    ownerName: '',
    phoneNumber: ''
  })

  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState('')

  const [logoFile, setLogoFile] = useState(null)
  const [uploadingLogo, setUploadingLogo] = useState(false)

  const [newEmail, setNewEmail] = useState('')
  const [emailChangeLoading, setEmailChangeLoading] = useState(false)
  const [emailChangeMessage, setEmailChangeMessage] = useState('')

  const [emailRemindersEnabled, setEmailRemindersEnabled] = useState(true)
  const [savingNotifications, setSavingNotifications] = useState(false)
  const [notificationMessage, setNotificationMessage] = useState('')
  const [notificationError, setNotificationError] = useState('')

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const response = await fetch(
          `${API_URL}/api/business-owners/me`,
          {
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
        )

        if (!response.ok) {
          throw new Error(
            `Unable to load business profile. HTTP ${response.status}`
          )
        }

        const data = await response.json()

        setProfile(data)

        setForm({
          businessName: data.businessName || '',
          ownerName: data.ownerName || '',
          phoneNumber: data.phoneNumber || ''
        })

      } catch (err) {
        setError(err.message || 'Unable to load business profile.')
      } finally {
        setLoading(false)
      }
    }

    loadProfile()
  }, [token])

useEffect(() => {
  const loadNotificationSettings = async () => {
    try {
      const response = await fetch(
        `${API_URL}/api/business-owners/me/notification-settings`,
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      )

      if (!response.ok) {
        throw new Error('Unable to load notification settings.')
      }

      const data = await response.json()
      setEmailRemindersEnabled(data.emailRemindersEnabled)

    } catch (err) {
      setNotificationError(err.message)
    }
  }

  loadNotificationSettings()
}, [token])
const handleChange = (event) => {
  const { name, value } = event.target

  setForm(previous => ({
    ...previous,
    [name]: value
  }))
}

const handleSave = async (event) => {
  event.preventDefault()

  setSaving(true)
  setError('')
  setMessage('')

  try {
    const response = await fetch(`${API_URL}/api/business-owners/me`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(form)
    })

    const data = await response.json()

    if (!response.ok) {
      throw new Error(data.message || 'Unable to update business profile.')
    }

    setProfile(data)
    setMessage('Business profile updated successfully.')

  } catch (err) {
    setError(err.message || 'Unable to update business profile.')
  } finally {
    setSaving(false)
  }
}
const handleUploadLogo = async () => {
  if (!logoFile) {
    setError('Please select a logo first.')
    return
  }

  setUploadingLogo(true)
  setError('')
  setMessage('')

  try {
    const formData = new FormData()
    formData.append('file', logoFile)

    const response = await fetch(`${API_URL}/api/business-owners/me/logo`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`
      },
      body: formData
    })

    const data = await response.json()

    if (!response.ok) {
      throw new Error(data.message || 'Unable to upload logo.')
    }

    setProfile(previous => ({
      ...previous,
      logoUrl: data.logoUrl
    }))

    setLogoFile(null)
    setMessage('Business logo uploaded successfully.')

  } catch (err) {
    setError(err.message || 'Unable to upload logo.')
  } finally {
    setUploadingLogo(false)
  }
}
const handleEmailChange = async (event) => {
  event.preventDefault()

  setEmailChangeLoading(true)
  setError('')
  setMessage('')
  setEmailChangeMessage('')

  try {
    const response = await fetch(
      `${API_URL}/api/business-owners/me/email`,
      {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          newEmail: newEmail.trim()
        })
      }
    )

    const data = await response.json()

    if (!response.ok) {
      throw new Error(data.message || 'Unable to request email change.')
    }

    setEmailChangeMessage(data.message)
    setNewEmail('')

  } catch (err) {
    setError(err.message || 'Unable to update email address.')
  } finally {
    setEmailChangeLoading(false)
  }
}
const saveNotificationSettings = async (enabled) => {
  setSavingNotifications(true)
  setNotificationError('')
  setNotificationMessage('')

  try {
    const response = await fetch(
      `${API_URL}/api/business-owners/me/notification-settings`,
      {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          emailRemindersEnabled: enabled
        })
      }
    )

    const data = await response.json()

    if (!response.ok) {
      throw new Error('Unable to save notification settings.')
    }

    setEmailRemindersEnabled(data.emailRemindersEnabled)
    setNotificationMessage('Notification settings saved successfully.')

  } catch (err) {
    setNotificationError(err.message)
  } finally {
    setSavingNotifications(false)
  }
}

  if (loading) {
    return <p>Loading business settings...</p>
  }

  if (error) {
    return <p className="login-error">{error}</p>
  }

  if (!profile) {
    return <p>Business profile not found.</p>
  }

  return (
    <section className="settings-page">

      <div className="section-heading">
        <div>
          <h2>Business Settings</h2>
          <p>View your business account information</p>
        </div>
      </div>

      <div className="settings-card">

        <div className="settings-avatar">
          {profile.businessName.charAt(0).toUpperCase()}
        </div>

        <h3>{profile.businessName}</h3>
        <p className="settings-subtitle">Business Profile</p>
        <div className="logo-upload-section">

          <h3>Business Logo</h3>

          {profile.logoUrl && (
            <img
              src={`${API_URL}${profile.logoUrl}`}
              alt="Business Logo"
              className="business-logo-preview"
            />
          )}

          <label>Choose Logo (PNG, JPG or WebP)</label>

          <input
            type="file"
            accept="image/png,image/jpeg,image/webp"
            onChange={(event) =>
              setLogoFile(event.target.files[0] || null)
            }
          />

          <button
            type="button"
            onClick={handleUploadLogo}
            disabled={uploadingLogo || !logoFile}
          >
            {uploadingLogo ? 'Uploading...' : 'Upload Logo'}
          </button>

          <p>Maximum file size: 2 MB</p>

        </div>

        <div className="settings-details">

          <div className="settings-field">
            <label>Business ID</label>
            <p>{profile.id}</p>
          </div>

          <div className="settings-field">
            <label>Email Address</label>
            <p>{profile.email}</p>
          </div>

          <form className="profile-form" onSubmit={handleSave}>

            <h3>Edit Business Profile</h3>

            <label>Business Name</label>
            <input
              type="text"
              name="businessName"
              value={form.businessName}
              onChange={handleChange}
              required
            />

            <label>Owner Name</label>
            <input
              type="text"
              name="ownerName"
              value={form.ownerName}
              onChange={handleChange}
              required
            />

            <label>Phone Number</label>
            <input
              type="tel"
              name="phoneNumber"
              value={form.phoneNumber}
              onChange={handleChange}
            />

            <button type="submit" disabled={saving}>
              {saving ? 'Saving...' : 'Save Profile'}
            </button>

          </form>

          {message && (
            <p className="success-message">{message}</p>
          )}

          {error && (
            <p className="login-error">{error}</p>
          )}
      <div className="email-change-section">

        <h3>Change Login Email</h3>

        <p>
          Enter a new email address. We'll send a verification link
          before changing your login email.
        </p>

        <form onSubmit={handleEmailChange}>

          <label>New Email Address</label>

          <input
            type="email"
            placeholder="Enter your new email address"
            value={newEmail}
            onChange={(event) => setNewEmail(event.target.value)}
            required
          />

          <button type="submit" disabled={emailChangeLoading}>
            {emailChangeLoading ? 'Sending...' : 'Send Verification Link'}
          </button>

        </form>

        {emailChangeMessage && (
          <p className="success-message">{emailChangeMessage}</p>
        )}

      </div>
      <div className="notification-settings-section">

        <h3>Notification Preferences</h3>

        <p>
          Choose how your business sends appointment reminders to customers.
        </p>

        {notificationMessage && (
          <p className="success-message">{notificationMessage}</p>
        )}

        {notificationError && (
          <p className="login-error">{notificationError}</p>
        )}

        <div className="notification-option">
          <div>
            <strong>Email Reminders</strong>
            <p>Automatically email customers before their appointments.</p>
          </div>

          <label>
            <input
              type="checkbox"
              checked={emailRemindersEnabled}
              disabled={savingNotifications}
              onChange={event =>
                saveNotificationSettings(event.target.checked)
              }
            />
            {savingNotifications
              ? 'Saving...'
              : emailRemindersEnabled
                ? 'Enabled'
                : 'Disabled'}
          </label>
        </div>

        <div className="notification-option">
          <div>
            <strong>WhatsApp Reminders</strong>
            <p>Send appointment reminders through WhatsApp.</p>
          </div>
          <span>Not connected</span>
        </div>

        <div className="notification-option">
          <div>
            <strong>SMS Reminders</strong>
            <p>Send appointment reminders through SMS.</p>
          </div>
          <span>Not connected</span>
        </div>

      </div>
      <LicenseActivation token={token} />

          <div className="booking-link-section">
            <h3>Your Public Booking Link</h3>

            <p>
              Share this link with customers so they can book appointments
              online.
            </p>

            <div className="booking-link-box">

              <input
                type="text"
                readOnly
                value={`${window.location.origin}/book/${profile.bookingSlug || profile.id}`}
              />

              <button
                type="button"
                onClick={() => {
                  navigator.clipboard.writeText(
                    `${window.location.origin}/book/${profile.bookingSlug || profile.id}`
                  )
                }}
              >
                Copy Link
              </button>

            </div>
          </div>


        </div>

      </div>

    </section>
  )
}

export default Settings
