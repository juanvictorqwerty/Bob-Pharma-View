import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import {Routes, Route, BrowserRouter} from 'react-router-dom'

import LandingPage from './LandingPage'

import './index.css'


createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>
)
